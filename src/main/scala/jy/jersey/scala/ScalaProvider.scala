package jy.jersey.scala

import javax.ws.rs.ext.Provider
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider
import java.lang.reflect.Type
import java.lang.annotation.Annotation
import java.io.{IOException, InputStream, OutputStream}
import javax.ws.rs.{WebApplicationException, Consumes, Produces}
import javax.ws.rs.core.{Response, MultivaluedMap, MediaType}
import javax.ws.rs.core.Response.Status
import scala.reflect.Manifest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature

/**
 * This provider enables scala objects to be read and written by jersey.  This uses jackson's scala module
 * This code is heavily borrowed/copied from codahale's jersey scala code.
 */

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class ScalaProvider[A] extends AbstractMessageReaderWriterProvider[A] {

  private val jsonMapper = new ObjectMapper with ScalaObjectMapper
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  
  def readFrom(klass: Class[A],
               genericType: Type,
               annotations: Array[Annotation],
               mediaType: MediaType,
               httpHeaders: MultivaluedMap[String, String],
               entityStream: InputStream) = {
    try {
      jsonMapper.readValue(entityStream)(Manifest.classType(klass))
    } catch {
      case e: JsonProcessingException => {
        throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity(e.getMessage)
          .build)
      }
    }
  }

  def isReadable(klass: Class[_],
                 genericType: Type,
                 annotations: Array[Annotation],
                 mediaType: MediaType) = mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)

  def writeTo(t: A,
              klass: Class[_],
              genericType: Type,
              annotations: Array[Annotation],
              mediaType: MediaType,
              httpHeaders: MultivaluedMap[String, AnyRef],
              entityStream: OutputStream) {
    jsonMapper.writeValue(entityStream, t)
  }

  def isWriteable(klass: Class[_],
                  genericType: Type,
                  annotations: Array[Annotation],
                  mediaType: MediaType) = mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)
}