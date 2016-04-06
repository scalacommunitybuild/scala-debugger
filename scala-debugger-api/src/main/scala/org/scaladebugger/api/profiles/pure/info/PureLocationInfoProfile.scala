package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{Location, Method, ReferenceType}
import org.scaladebugger.api.profiles.traits.info.{LocationInfoProfile, MethodInfoProfile, ReferenceTypeInfoProfile}

/**
 * Represents a pure implementation of a location profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param location The reference to the underlying JDI location
 */
class PureLocationInfoProfile(
  private val location: Location
) extends LocationInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Location = location

  /**
   * Retrieves the code position within the location's method.
   *
   * @return The code position, or -1 if not available
   */
  override def getCodeIndex: Long = location.codeIndex()

  /**
   * Retrieves the line number associated with the location.
   *
   * @return The line number, or -1 if not available
   */
  override def getLineNumber: Int = location.lineNumber()

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return The identifying name
   */
  override def getSourceName: String = location.sourceName()

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return The source path
   */
  override def getSourcePath: String = location.sourcePath()

  /**
   * Retrieves the reference type information for the type containing this
   * location.
   *
   * @return The reference type information
   */
  override def getDeclaringType: ReferenceTypeInfoProfile =
    newReferenceTypeProfile(location.declaringType())

  /**
   * Retrieves the method information for the method containing this location.
   *
   * @return The method information
   */
  override def getMethod: MethodInfoProfile =
    newMethodProfile(location.method())

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    referenceType
  )

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(method)
}
