import groovy.transform.*

@TupleConstructor @ToString
class Column {
  String tablePName
  String tableLName
  String columnPName
  String columnLName
}
