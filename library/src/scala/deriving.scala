package scala

object deriving {

  /** Mirrors allows typelevel access to enums, case classes and objects, and their sealed parents.
   */
  sealed trait Mirror {

    /** The mirrored *-type */
    type MirroredMonoType

    /** The name of the type */
    type MirroredLabel <: String
  }

  object Mirror {

    /** The Mirror for a sum type */
    trait Sum extends Mirror { self =>

      /** The types of the alternatives */
      type MirroredElemTypes <: Tuple

      /** The ordinal number of the case class of `x`. For enums, `ordinal(x) == x.ordinal` */
      def ordinal(x: MirroredMonoType): Int
    }

    /** The Mirror for a product type */
    trait Product extends Mirror {

      /** The types of the product elements */
      type MirroredElemTypes <: Tuple

      /** The names of the product elements */
      type MirroredElemLabels <: Tuple

      /** Create a new instance of type `T` with elements taken from product `p`. */
      def fromProduct(p: scala.Product): MirroredMonoType
    }

    trait Singleton extends Product {
      type MirroredMonoType = this.type
      type MirroredElemTypes = Unit
      type MirroredElemLabels = Unit
      def fromProduct(p: scala.Product) = this
    }

    /** A proxy for Scala 2 singletons, which do not inherit `Singleton` directly */
    class SingletonProxy(val value: AnyRef) extends Product {
      type MirroredMonoType = value.type
      type MirroredElemTypes = Unit
      type MirroredElemLabels = Unit
      def fromProduct(p: scala.Product) = value
    }

    type Of[T]        = Mirror { type MirroredMonoType = T }
    type ProductOf[T] = Mirror.Product { type MirroredMonoType = T }
    type SumOf[T]     = Mirror.Sum { type MirroredMonoType = T }
  }

  /** Helper class to turn arrays into products */
  class ArrayProduct(val elems: Array[AnyRef]) extends Product {
    def this(size: Int) = this(new Array[AnyRef](size))
    def canEqual(that: Any): Boolean = true
    def productElement(n: Int) = elems(n)
    def productArity = elems.length
    override def productIterator: Iterator[Any] = elems.iterator
    def update(n: Int, x: Any) = elems(n) = x.asInstanceOf[AnyRef]
  }

  /** The empty product */
  object EmptyProduct extends ArrayProduct(Array[AnyRef]())

  /** Helper method to select a product element */
  def productElement[T](x: Any, idx: Int) =
    x.asInstanceOf[Product].productElement(idx).asInstanceOf[T]
}