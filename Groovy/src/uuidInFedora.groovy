#!/usr/bin/env groovy

System.in.eachLine() { line ->
  def uuid = line
  def url = new URL("http://fedora:8080/fedora/get/" + uuid)
  def connection = url.openConnection()
  connection.setRequestMethod("GET")
  connection.connect()
  println uuid + ":"  + connection.responseCode
}
