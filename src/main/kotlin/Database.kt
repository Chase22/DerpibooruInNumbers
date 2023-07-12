import java.sql.DriverManager
import java.sql.ResultSet

private const val connectionString = "jdbc:postgresql://localhost:5432/derpibooru"
private const val username = "postgres"
private const val password = "derpibooru"

val connection by lazy {
    DriverManager.getConnection(connectionString, username, password)
}

fun ResultSet.toMapSequence() = generateSequence {
    if (!next()) return@generateSequence null

    (0..metaData.columnCount).associate {
        metaData.getColumnName(it) to getObject(it)
    }
}