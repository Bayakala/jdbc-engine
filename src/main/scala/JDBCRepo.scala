import com.datatech.sdp.jdbc.engine.JDBCEngine._
import com.datatech.sdp.jdbc.engine.{JDBCQueryContext, JDBCUpdateContext}
import scalikejdbc._
import akka.stream.ActorMaterializer
import com.datatech.sdp.result.DBOResult.DBOResult
import akka.stream.scaladsl._
import scala.concurrent._


object Repo {

  class JDBCRepo(ec: ExecutionContextExecutor, mat: ActorMaterializer) {
    implicit val ec1 = ec
    implicit val mat1 = mat
    def query[R](db: String, sqlText: String, toRow: WrappedResultSet => R): Source[R,_] = {
      //construct the context
      val ctx = JDBCQueryContext(
        dbName = Symbol(db),
        statement = sqlText
      )
      jdbcAkkaStream(ctx,toRow)
    }
    def update(db: String, sqlTexts: Seq[String]): DBOResult[Seq[Long]] = {
      val ctx = JDBCUpdateContext(
        dbName = Symbol(db),
        statements = sqlTexts
      )
      jdbcTxUpdates(ctx)
    }
    def bulkInsert[P](db: String, sqlText: String, prepParams: P => Seq[Any], params: Source[P,_]) = {
      val insertAction = JDBCActionStream(
        dbName = Symbol(db),
        parallelism = 4,
        processInOrder = false,
        statement = sqlText,
        prepareParams = prepParams
      )
      params.via(insertAction.performOnRow).to(Sink.ignore).run()
    }

  }





}