fun main() {
    val resultSet = connection.createStatement().executeQuery("SELECT * from tag_aliases_view")
    val columns = resultSet.toMapSequence()
}