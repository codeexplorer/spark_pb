package com.codeexplorer.sparkpb

import org.apache.spark.sql.{Dataset, SparkSession}
import com.codeexplorer.sparkpb.sample._

object sparkPB {
  def main(args: Array[String]): Unit = {

    def appName: String = "sparkPB"

    val spark = SparkSession.builder
      .appName(appName)
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    SampleProtoUdt.register()

    val submetric = SubMetric().update(
      _.smid := 1,
      _.violationType := SubMetric.violation_type_t.NON_COMPLIANT,
      _.metricReason := SubMetric.metric_reason_t.REASON_1,
      _.value := 100,
      _.timestamp := 12345,
      _.sessionCnt := 1
    )
    val metric = Seq(Metric("m1", Seq(submetric)))

    val metricDF = spark.sqlContext.createDataset(metric)
    metricDF.show
  }

}