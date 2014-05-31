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

def book = GExcel.open('table.xlsx')
def sheet = book[0]
def from = [args[0], '']
def joins = [] // [tablePName, tableLName, ons = [joinTable, columnPName, columnLName]]
def selects = []
sheet.eachWithIndex {row, i ->
  if (i <= 1) {
    // skip header row
    return
  }
  def tablePName = row[0].evaluatedValue
  def tableLName = row[1].evaluatedValue
  def columnPName = row[2].evaluatedValue
  if (!args.contains(tablePName)) {
    return 
  }
  if (from[1].empty && !tableLName.empty && from[0] == tablePName) {
    from[1] = tableLName
  }
  if (from[0] != tablePName && !joins.collect{it[0]}.contains(tablePName)) {
    joins << [tablePName, tableLName, []]
  }
  if (selects.empty || !selects.collect{it[2]}.contains(columnPName)) {
    selects << row.collect{it.evaluatedValue}
  } else {
    def joinTable = selects.find{it[2] == columnPName}[0]
    def ons = joins.find{it[0] == tablePName}[2]
    ons << [joinTable, columnPName, row[3].evaluatedValue]
  }
}

def templateFileName = 'template.sql'
def f = new File(templateFileName)
def engine = new groovy.text.SimpleTemplateEngine()
def binding = ['from' : from, 'selects' :  selects, 'joins' : joins ]
def template = engine.createTemplate(f).make(binding)

def date = new Date().format('yyyyMMdd_HHmmss')
def resultFileName = date + '.sql'
// def result = new File(resultFileName)
// result.write(template.toString(), 'SJIS')
println template.toString()