package com.codeexplorer.sparkpb

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.types.StructType
import com.codeexplorer.sparkpb.sample._
import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import org.apache.spark.sql.functions._
import frameless._

object sparkPB {

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

    implicit val violationToInt: Injection[SubMetric.violation_type_t, Int] =
      new Injection[SubMetric.violation_type_t, Int] {
        def apply(d: SubMetric.violation_type_t): Int = d match {
          case SubMetric.violation_type_t.NON_COMPLIANT => 0
          case SubMetric.violation_type_t.COMPLIANT => 1
        }

        def invert(l: Int): SubMetric.violation_type_t = l match {
          case 0 => SubMetric.violation_type_t.NON_COMPLIANT
          case 1 => SubMetric.violation_type_t.COMPLIANT
        }
      }

    implicit val reasonToInt: Injection[SubMetric.metric_reason_t, Int] =
      new Injection[SubMetric.metric_reason_t, Int] {
        def apply(d: SubMetric.metric_reason_t): Int = d match {
          case SubMetric.metric_reason_t.REASON_1 => 1
          case SubMetric.metric_reason_t.REASON_2 => 2
          case SubMetric.metric_reason_t.REASON_3 => 3
          case SubMetric.metric_reason_t.REASON_4 => 4
        }

        def invert(l: Int): SubMetric.metric_reason_t = l match {
          case 1 => SubMetric.metric_reason_t.REASON_1
          case 2 => SubMetric.metric_reason_t.REASON_2
          case 3 => SubMetric.metric_reason_t.REASON_3
          case 4 => SubMetric.metric_reason_t.REASON_4
        }
      }

    val metric: Metric = Metric("m1", Seq(submetric))

    // Direct creation of Datasets with enums fails, uncomment below to see
    //val metricDFdirect = spark.sqlContext.createDataset(Seq(metric))
    //metricDFdirect.show

    // But typed dataset passes
    val metricTDS = TypedDataset.create(Seq(metric))
    metricTDS.toDF.show

    // Try to Construct metric from byteArray
    val metricBytes = metric.toByteArray

    //How to make the following work with injection
    val byteDF = spark.sparkContext.parallelize(Seq(metricBytes)).toDF.as[Array[Byte]]
    val byteMetric = byteDF.map(row => Metric.parseFrom(row))
    byteMetric.show(false)
  }
}