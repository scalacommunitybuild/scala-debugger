package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfoProfile, ObjectInfoProfile, PrimitiveInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

object PureValueInfoProfile {
  val DefaultNullTypeName = "null"
}

/**
 * Represents a pure implementation of a value profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            value
 * @param _value The reference to the underlying JDI value
 */
class PureValueInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val _value: Value
) extends ValueInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Value = _value

  /**
   * Returns the type name of this value.
   *
   * @return The type name (typically a fully-qualified class name)
   */
  override def typeName: String =
    if (!isNull) _value.`type`().name()
    else PureValueInfoProfile.DefaultNullTypeName

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[AssertionError]
  override def toArray: ArrayInfoProfile = {
    assert(isArray, "Value must be an array!")
    newArrayProfile(_value.asInstanceOf[ArrayReference])
  }

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  override def toLocalValue: Any = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    if (!isNull) _value.value()
    else null
  }

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[AssertionError]
  override def toObject: ObjectInfoProfile = {
    assert(isObject, "Value must be an object!")
    newObjectProfile(_value.asInstanceOf[ObjectReference])
  }

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  override def toPrimitive: PrimitiveInfoProfile = {
    assert(isPrimitive, "Value must be a primitive!")
    newPrimitiveProfile(_value.asInstanceOf[PrimitiveValue])
  }

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  override def isPrimitive: Boolean =
    !isNull && _value.isInstanceOf[PrimitiveValue]

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  override def isArray: Boolean =
    !isNull && _value.isInstanceOf[ArrayReference]

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  override def isObject: Boolean =
    !isNull && _value.isInstanceOf[ObjectReference]

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  override def isString: Boolean =
    !isNull && _value.isInstanceOf[StringReference]

  /**
   * Returns whether or not this value is void.
   *
   * @return True if void, otherwise false
   */
  override def isVoid: Boolean = !isNull && _value.isInstanceOf[VoidValue]

  /**
   * Returns whether or not this value is null.
   *
   * @return True if null, otherwise false
   */
  override def isNull: Boolean = _value == null

  protected def newPrimitiveProfile(primitiveValue: PrimitiveValue): PrimitiveInfoProfile =
    new PurePrimitiveInfoProfile(scalaVirtualMachine, primitiveValue)

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new PureObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newArrayProfile(arrayReference: ArrayReference): ArrayInfoProfile =
    new PureArrayInfoProfile(scalaVirtualMachine, arrayReference)()
}
