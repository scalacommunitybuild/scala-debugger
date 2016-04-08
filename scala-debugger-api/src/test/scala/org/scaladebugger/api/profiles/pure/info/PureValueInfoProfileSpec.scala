package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfoProfile, ObjectInfoProfile, PrimitiveInfoProfile, ValueInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureValueInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockValue = mock[Value]
  private val mockNewPrimitiveProfile = mockFunction[PrimitiveValue, PrimitiveInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
  private val mockNewArrayProfile = mockFunction[ArrayReference, ArrayInfoProfile]

  describe("PureValueInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockValue

        val pureValueInfoProfile = new PureValueInfoProfile(mockValue)

        val actual = pureValueInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#typeName") {
      it("should return the type name of the value") {
        val expected = "some.type.name"

        val mockType = mock[Type]
        (mockType.name _).expects().returning(expected).once()

        val mockValue = mock[Value]
        (mockValue.`type` _).expects().returning(mockType).once()

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockValue
        )
        val actual = pureValueInfoProfile.typeName

        actual should be (expected)
      }

      it("should return the default null type name if the value is null") {
        val expected = PureValueInfoProfile.DefaultNullTypeName

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.typeName

        actual should be (expected)
      }
    }

    describe("#toArray") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArray
        }
      }

      it("should throw an assertion error if the value is not an array") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArray
        }
      }

      it("should return an array reference wrapped in a profile") {
        val expected = mock[ArrayInfoProfile]
        val mockArrayReference = mock[ArrayReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockArrayReference
        ) {
          override protected def newArrayProfile(
            arrayReference: ArrayReference
          ): ArrayInfoProfile = mockNewArrayProfile(arrayReference)
        }

        mockNewArrayProfile.expects(mockArrayReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toArray

        actual should be (expected)
      }
    }

    describe("#toPrimitive") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitive
        }
      }

      it("should throw an assertion error if the value is not an primitive") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitive
        }
      }

      it("should return an primitive reference wrapped in a profile") {
        val expected = mock[PrimitiveInfoProfile]
        val mockPrimitiveValue = mock[PrimitiveValue]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockPrimitiveValue
        ) {
          override protected def newPrimitiveProfile(
            primitiveValue: PrimitiveValue
          ): PrimitiveInfoProfile = mockNewPrimitiveProfile(primitiveValue)
        }

        mockNewPrimitiveProfile.expects(mockPrimitiveValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toPrimitive

        actual should be (expected)
      }
    }

    describe("#toObject") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObject
        }
      }

      it("should throw an assertion error if the value is not an object") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObject
        }
      }

      it("should return an object reference wrapped in a profile") {
        val expected = mock[ObjectInfoProfile]
        val mockObjectReference = mock[ObjectReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockObjectReference
        ) {
          override protected def newObjectProfile(
            arrayReference: ObjectReference
          ): ObjectInfoProfile = mockNewObjectProfile(arrayReference)
        }

        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toObject

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should return null if the value is null") {
        val expected: Any = null

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }

      it("should convert the remote value to its underlying value") {
        val expected = "some value"
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockStringReference
        )

        (mockStringReference.value _).expects().returning(expected).once()
        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#isPrimitive") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return false if the value is not a primitive") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is a primitive") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }
    }

    describe("#isVoid") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return false if the value is not void") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return true if the value is void") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[VoidValue]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }
    }

    describe("#isObject") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return false if the value is not a object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return true if the value is a object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[ObjectReference]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }
    }

    describe("#isString") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return false if the value is not a string") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return true if the value is a string") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[StringReference]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }
    }

    describe("#isArray") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return false if the value is not a array") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return true if the value is a array") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[ArrayReference]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }
    }

    describe("#isNull") {
      it("should return true if the value is null") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          null
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }

      it("should return false if the value is not null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mock[Value]
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }
    }
  }
}
