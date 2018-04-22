package com.codeexplorer.sparkpb

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.types.StructType
import com.codeexplorer.sparkpb.sample._
import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import org.apache.spark.sql.functions._
import scalapb._
import frameless._
import java.sql.Timestamp
object sparkPB {

  case class ArrayByteTimestamp(timestamp: Timestamp, byteArray:Array[Byte])
  case class MetricTimestamp(timestamp: Timestamp, metric:Metric)

  implicit def enumToIntInjection[E <: GeneratedEnum](implicit cmp: GeneratedEnumCompanion[E]) =
        new Injection[E, Int] {
          override def apply(a: E): Int = a.value
          override def invert(value: Int): E = cmp.fromValue(value)
        }

  implicit val timestamptoLong: Injection[java.sql.Timestamp, Long] =
        new Injection[java.sql.Timestamp, Long] {
          def apply (ts: java.sql.Timestamp): Long = ts.getTime()
          def invert (l: Long) = new Timestamp(l)
        }

  def main(args: Array[String]): Unit = {

    def appName: String = "sparkPB"

    implicit val spark = SparkSession.builder
      .appName(appName)
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    SampleProtoUdt.register()

    //Construct metric from constructors
    val submetric = SubMetric().update(
      _.smid := 1,
      _.violationType := SubMetric.violation_type_t.NON_COMPLIANT,
      _.metricReason := SubMetric.metric_reason_t.REASON_1,
      _.value := 100,
      _.timestamp := 12345,
      _.sessionCnt := 1
    )

    val metric: Metric = Metric("m1", Seq(submetric))

    // Direct creation of Datasets with enums fails, uncomment below to see
    //val metricDFdirect = spark.sqlContext.createDataset(Seq(metric))
    //metricDFdirect.show

    // But typed dataset passes
    val metricTDS = TypedDataset.create(Seq(metric))
    metricTDS.toDF.show

    // Try to Construct metric from byteArray
    val metricBytes = metric.toByteArray
    val timestamp = new Timestamp(10000000000L)

    //How to make the following work with injection
    val byteDF = spark.sparkContext.parallelize(Seq(ArrayByteTimestamp(timestamp, metricBytes))).toDF("timestamp","byteArray").as[ArrayByteTimestamp]
    
    import frameless.syntax._
    val byteMetric:TypedDataset[MetricTimestamp] = TypedDataset.create(byteDF.rdd.map(el=> MetricTimestamp(el.timestamp, Metric.parseFrom(el.byteArray))))
    byteMetric.show().run()

    spark.stop()
  }
}