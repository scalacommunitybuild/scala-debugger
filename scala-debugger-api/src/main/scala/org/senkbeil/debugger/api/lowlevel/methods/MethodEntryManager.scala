package org.senkbeil.debugger.api.lowlevel.methods

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, MethodEntryRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for method entry requests.
 *
 * @param eventRequestManager The manager used to create method entry requests
 */
class MethodEntryManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  /** The arguments used to lookup method entry requests: (Class, Method) */
  type MethodEntryArgs = (String, String)

  /** The key used to lookup method entry requests */
  type MethodEntryKey = String

  private val methodEntryArgsToRequestId =
    new ConcurrentHashMap[MethodEntryArgs, MethodEntryKey]().asScala
  private val methodEntryRequests =
    new ConcurrentHashMap[MethodEntryKey, MethodEntryRequest]().asScala

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests in the form of
   *         (class name, method name)
   */
  def methodEntryRequestList: Seq[MethodEntryArgs] =
    methodEntryArgsToRequestId.keySet.toSeq

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests by id
   */
  def methodEntryRequestListById: Seq[MethodEntryKey] =
    methodEntryRequests.keySet.toSeq

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodEntryRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[MethodEntryKey] = {
    val request = Try(eventRequestManager.createMethodEntryRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      methodEntryArgsToRequestId.put((className, methodName), requestId)
      methodEntryRequests.put(requestId, request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodEntryRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[MethodEntryKey] = {
    createMethodEntryRequestWithId(
      newRequestId(),
      className,
      methodName,
      extraArguments: _*
    )
  }

  /**
   * Determines if a method entry request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if a method entry request exists, otherwise false
   */
  def hasMethodEntryRequest(className: String, methodName: String): Boolean = {
    methodEntryArgsToRequestId
      .get((className, methodName))
      .exists(hasMethodEntryRequestWithId)
  }

  /**
   * Determines if a method entry request exists with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if a method entry request exists, otherwise false
   */
  def hasMethodEntryRequestWithId(requestId: String): Boolean = {
    methodEntryRequests.contains(requestId)
  }

  /**
   * Retrieves the method entry request for the specific class and method.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return Some method entry request if it exists, otherwise None
   */
  def getMethodEntryRequest(
    className: String,
    methodName: String
  ): Option[MethodEntryRequest] = {
    methodEntryArgsToRequestId
      .get((className, methodName))
      .flatMap(getMethodEntryRequestWithId)
  }

  /**
   * Retrieves the method entry request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method entry request if it exists, otherwise None
   */
  def getMethodEntryRequestWithId(
    requestId: String
  ): Option[MethodEntryRequest] = {
    methodEntryRequests.get(requestId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodEntryRequest(
    className: String,
    methodName: String
  ): Boolean = {
    methodEntryArgsToRequestId.get((className, methodName))
      .exists(removeMethodEntryRequestWithId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param requestId The id of the request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodEntryRequestWithId(
    requestId: String
  ): Boolean = {
    // Remove request with given id
    val request = methodEntryRequests.remove(requestId)

    // Reverse-lookup arguments to remove argsToId mapping
    methodEntryArgsToRequestId.find(_._2 == requestId).map(_._1)
      .foreach(methodEntryArgsToRequestId.remove)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
