import os
import sys
import os.path
import xlrd
import json
import codecs

rootdir	 = "Card/"
jsdir = "json/"

def FloatToString (aFloat):
    if type(aFloat) != float:
        return ""
    strTemp = str(aFloat)
    strList = strTemp.split(".")
    if len(strList) == 1 :
        return strTemp
    else:
        if strList[1] == "0" :
            return strList[0]
        else:
            return strTemp
     
def table2jsn(table, jsonfilename):
    nrows = table.nrows
    ncols = table.ncols
    f = codecs.open(jsonfilename,"w","utf-8")
    f.write(u"{\n")
    for r in range(nrows-1):
        if r == 0:
        	continue
        CellObj = table.cell_value(r+1,0)
        print FloatToString(CellObj)
        f.write(u"\t\""+FloatToString(CellObj)+u"\":{ ")
        for c in range(ncols):
            strCellValue = u""
            CellObj = table.cell_value(r+1,c)
            if type(CellObj) == unicode:
                strCellValue = CellObj
            elif type(CellObj) == float:
                strCellValue = FloatToString(CellObj)
            else:
                strCellValue = str(CellObj)
            strTmp = u"\""  + table.cell_value(1,c) + u"\":\""+ strCellValue + "\""
            if c< ncols-1:
                strTmp += u", "
            f.write(strTmp)
        f.write(u" }")
        if r < nrows-2:
            f.write(u",")
        f.write(u"\n")
    f.write(u"}\n")
    f.close()
    print "Create ",jsonfilename," OK"
    return

def convert_csv_to_js(filepath, isMulti):
	src = filepath
	dist = src.replace(rootdir, jsdir)
	dist = dist.replace(".xls", ".json")
	data = xlrd.open_workbook(filepath)
	desttable = data.sheet_by_index(0)
	table2jsn(desttable, dist)

def convert_all(filepath):
	if filepath[-4:] != ".xls":
		return
	pos = filepath.rfind("/")
	if pos == -1:
		return

	filename = filepath[pos + 1:-4]
	isMulti = False
	if filename.find("_Multi") != -1:
		isMulti = True

	if filename.find("_Common") != -1:
		convert_csv_to_js(filepath, isMulti)
	elif filename.find("_Client") != -1:
		convert_csv_to_js(filepath, isMulti)
	else:
		return


def walk_path(path, func):
	allfile = []
	def callback(arg, directory, files):
		print(arg, directory, files)
		js_path = directory.replace(rootdir, jsdir)
		if not os.path.isdir(js_path):
			os.makedirs(js_path)
		
		for filename in files:
			file_abs = directory + '/' + filename
			if os.path.isfile(file_abs):
				allfile.append(file_abs)

	os.path.walk(path, callback, None)
	for f in allfile:
		if f[-4:] == ".xls":
			convert_all(f)
	
if __name__ == "__main__":
	print(sys.argv)
	if len(sys.argv) == 3:
		print(sys.argv)
		luadir = sys.argv[1]
		pydir = sys.argv[2]
	walk_path(rootdir, convert_all)
