import java.io.InputStream

import com.datatech.sdp.jdbc.config.ConfigDBsWithEnv
import com.datatech.sdp.jdbc.engine.JDBCEngine._
import com.datatech.sdp.jdbc.engine.{JDBCQueryContext, JDBCUpdateContext}

import scala.util._
import scalikejdbc._
import java.time._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.datatech.sdp.result.DBOResult.DBOResult
import com.datatech.sdp.file.Streaming._
import Repo._
import akka.stream.scaladsl.Sink
object JDBCCrud extends App {

  implicit val jdbcsys = ActorSystem("cqlSystem")
  val mat = ActorMaterializer()
  val ec = jdbcsys.dispatcher
  import monix.execution.Scheduler.Implicits.global

  case class Cls(
     clscode: String,
     clsname: String
  )

  val toCls: WrappedResultSet => Cls = rs => Cls(
    clscode = rs.get("CLSCODE"),
    clsname = rs.get("CLSNAME")
  )
  ConfigDBsWithEnv("prod").setup('termtxns)
  ConfigDBsWithEnv("prod").setup('crmdb)
  ConfigDBsWithEnv("prod").loadGlobalSettings()

  val repo = new JDBCRepo(ec,mat)
  val rows = repo.query[Cls]("termtxns","SELECT CLSCODE,CLSNAME FROM CLASS", toCls)
  val sink = Sink.foreach[Cls]{ r =>
    println(s"${r.clscode},${r.clsname}")
  }
  val toParam: Cls => Seq[Any] = c => Seq(c.clscode,c.clsname)
 // val ret = rows.runForeach(r => println(s"${r.clscode},${r.clsname}"))(mat)      //.to(sink).run()(mat)
  val createCls: String =
    """
      |create table SIMPLECLS(
      |CODE varchar(10) not null,
      |NAME varchar(100) null
      |)
      |""".stripMargin

  repo.update("crmdb",Seq("drop table SIMPLECLS",createCls))

  repo.update("crmdb",Seq(createCls))

  repo.bulkInsert("crmdb","insert into SIMPLECLS(CODE,NAME) VALUES(?,?)",toParam,rows)

  val dropSQL: String ="""
      drop table members
    """

  val createSQL: String ="""
    create table members (
      id int primary key,
      name varchar(30) not null,
      description varchar(1000),
      birthday date,
      created_at datetime not null
    )"""

  var ctx = JDBCUpdateContext('crmdb)
  try {
    ctx = ctx.setDDLCommand(dropSQL)
      .appendDDLCommand(createSQL)
  }
  catch {
    case e: Exception => println(e.getMessage)
  }

  val resultCreateTable = jdbcExecuteDDL(ctx)(ec)


  resultCreateTable.value.value.runToFuture.map {
    eos =>
      eos match {
      case Right(os) => println(os.getOrElse("none"))
      case Left(err) => println(s"Left Error: $err")
    }
  }
  scala.io.StdIn.readLine()

  val insertSQL = "insert into members(id,name,birthday,description,created_at) values (?, ?, ?, ?, ?)"
  val dateCreated = LocalDateTime.now

  import scala.concurrent.duration._
  val fileName = "c:\\scala\\pics\\stop.ico"
 // val fis = FileToInputStream(fileName, 3 seconds)

  ctx = JDBCUpdateContext('crmdb)
  try {
    ctx = ctx.setBatchCommand(insertSQL)
      .appendBatchParameters(
      1, "John", LocalDate.of(2008,3,1),"youngest user",dateCreated)
      .appendBatchParameters(
      2,"peter", None, "no birth date", dateCreated)
      .appendBatchParameters(
        3,"susan", None, "no birth date", dateCreated)
  //    .setBatchReturnGeneratedKeyOption(JDBCUpdateContext.RETURN_GENERATED_KEYVALUE)
  }
  catch {
    case e: Exception => println(e.getMessage)
  }

  var resultInserts = jdbcBatchUpdate[List](ctx)(ec)

  resultInserts.value.value.runToFuture.map {
    eol =>
      eol match {
        case Right(ol) => ol match {
          case Some(ll) => println(s"return value: ${ll.asInstanceOf[List[Long]]}")
          case None => println("nothing returned!")
        }

        case Left(err) => println(s"Left Error: $err")
      }
  }
  scala.io.StdIn.readLine()

/*
  val updateSQL = "update members set description = ? where id < ?"
  ctx = JDBCUpdateContext('mssql)
  try {
    ctx = ctx.setUpdateCommand(JDBCUpdateContext.RETURN_GENERATED_KEYVALUE,insertSQL,
      "max", None, "no birth date", dateCreated, None)
      .appendUpdateCommand(JDBCUpdateContext.RETURN_UPDATED_COUNT, updateSQL, "id++", 10)
      .appendUpdateCommand(JDBCUpdateContext.RETURN_UPDATED_COUNT,"delete members where id = 1")
  }
  catch {
    case e: Exception => println(e.getMessage)
  }
  var resultUpdates = jdbcTxUpdates[Vector](ctx)(ec)

  resultUpdates.value.value.runToFuture.map {
    eol =>
      eol match {
        case Right(ol) => ol match {
          case Some(ll) => println(s"return value: ${ll.asInstanceOf[Vector[Long]]}")
          case None => println("nothing returned!")
        }

        case Left(err) => println(s"Left Error: $err")
      }
  }
  scala.io.StdIn.readLine()
*/

  //data model
  case class Member(
                     id: Long,
                     name: String,
                     description: Option[String] = None,
                     birthday: Option[LocalDate] = None,
                     createdAt: LocalDateTime)
//                     picture: InputStream)

  //data row converter
  val toMember = (rs: WrappedResultSet) => Member(
    id = rs.long("id"),
    name = rs.string("name"),
    description = rs.stringOpt("description"),
    birthday = rs.localDateOpt("birthday"),
    createdAt = rs.localDateTime("created_at")
  //  picture = rs.binaryStream("picture")
  )

  val ctx3 = JDBCQueryContext(
    dbName = 'crmdb,
    statement = "select * from members",
    queryTimeout = Some(1000)
  )

  val vecMember: DBOResult[Vector[Member]] = jdbcQueryResult[Vector,Member](ctx3,toMember)
  vecMember.value.value.runToFuture.map {
    eovm =>
      eovm match {
        case Right(ovm) => ovm match {
          case Some(vm) =>
            vm.foreach {row =>
              println(s"id: ${row.id} name: ${row.name}")
              println(s"name: ${row.name}")
              /*
              if (row.picture == null)
                println("picture empty")
              else {

                val fname = s"c:\\scala\\pics\\pic${row.id}.png"
                InputStreamToFile(row.picture,fname)
                println(s"picture saved to $fname!")

              }*/
            }
          case None => println("nothing returned!")
        }
        case Left(err) => println(s"Left Error: $err")
      }
  }

  scala.io.StdIn.readLine()
  jdbcsys.terminate()

}
