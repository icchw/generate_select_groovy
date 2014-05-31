/**
 * args[0] input filename
 * args[1] template filename
 * args[2] character code (default: UTF-8)
 * depends on:
 *   dom4j-1.6.1.jar
 *   gexcelapi-0.3-SNAPSHOT.jar
 *   poi-3.8.jar
 *   poi-ooxml-3.8.jar
 *   poi-ooxml-schemas-3.8.jar
 *   xmlbeans-2.3.0.jar
 */

import org.apache.poi.ss.usermodel.*
import org.jggug.kobo.gexcelapi.*
import Column


// expand Cell class
Cell.metaClass.define {
  getEvaluatedValue{
    switch (delegate.cellType) {
      case Cell.CELL_TYPE_FORMULA:
        def workbook = delegate.sheet.workbook
        return workbook.getCreationHelper().createFormulaEvaluator().evaluateInCell(delegate).value
      default :
        return delegate.value
    }
  }
}

// extract columns info from excel file
def getVal(cell) {
  if (cell == null) {
    return ''
  }
  return cell.evaluatedValue
}
def extractColumns(fileName) {
  def columns = []
  def book = GExcel.open(fileName)
  def sheet = book[0]
  sheet.eachWithIndex {row, i ->
    if (i <= 1) {
      // skip header row
      return
    }
    def tablePName = row[0].evaluatedValue
    if (!args.contains(tablePName)) {
      return 
    }
    columns << new Column(getVal(row[0]), getVal(row[1]), getVal(row[2]), getVal(row[3]))
  }
  return columns
}

def columns = extractColumns('table.xlsx')
def selects = []          // [tablePName, tableLName, columnPName, columnLName]
def from = [args[0], '']  // [tablePName. tableLName]
def joins = []            // [tablePName, tableLName, ons = [joinTable, columnPName, columnLName]]
args.eachWithIndex {tablePName, i -> 
  columns.each {c ->
    if (c.tablePName != tablePName) {
      return
    }
    if (i == 0 && from[1].empty && !c.tableLName.empty) {
      from[1] = c.tableLName
    }
    if (i != 0) {
      def joinTable = joins.find{it[0] == tablePName}
      if (joinTable == null) {
        joinTable = [c.tablePName, '', []]
        joins << joinTable
      }
      if (joinTable[1].empty && !c.tableLName.empty) {
        joinTable[1] = c.tableLName
      }
    }
    if (selects.empty || !selects.collect{it.columnPName}.contains(c.columnPName)) {
      selects << c
    } else {
      def joinTable = selects.find{it.columnPName == c.columnPName}.tablePName
      def ons = joins.find{it[0] == tablePName}[2]
      ons << [joinTable, c.columnPName, c.columnLName]
    }
  }
}
def templateFileName = 'template.sql'
def f = new File(templateFileName)
def engine = new groovy.text.SimpleTemplateEngine()
def binding = ['from' : from, 'selects' :  selects, 'joins' : joins ]
def template = engine.createTemplate(f).make(binding)
def date = new Date().format('yyyyMMdd_HHmmss')
def resultFileName = date + '.sql'
def result = new File(resultFileName)
result.write(template.toString(), 'SJIS')
