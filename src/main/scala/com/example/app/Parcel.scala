package com.example.app

case class Parcel(weight: Option[Int], width: Option[Int], height: Option[Int], length: Option[Int])

object Parcel
{
  def isAnyFieldNone (parcel: Parcel): Boolean =
    parcel.weight.isEmpty || parcel.width.isEmpty || parcel.height.isEmpty || parcel.length.isEmpty
}
