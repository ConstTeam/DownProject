package util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * 对象和Excel一条记录的映射
 * 
 * 注意：
 * 1、已知能获取数字和文本类型单元格的内容，其他类型单元格的内容只能尝试读取文本内容
 * 2、待映射对象的属性要么是公有的，要么是非公有的则必须提供标准的get/set访问器
 * 3、Excel中的标题行必须是文本型的单元格
 * 4、如果Excel单元格的类型是数字型的，而映射的对象属性是文本类型的，那么获取到的内容可能不匹配。
 *    例如：Excel单元格格式Numeric，内容12345。赋值到对象属性中的内容是"12345.0"
 * 5、如果键是String类型的，而以数字类型进行获取，是获取不到内容的。
 *    这是因为Object泛型下的隐式转换将基本类型int转换为Integer,与String并非一个对象
 * 6、已知支持版本为2007 ~ 2010后缀为.xlsx格式的Excel
 * 
 */
public class ExcelXlsxLoader {
	
	/**
	 * 加载模型配置文件，从第1个Sheet页，以第1列作为主键，
	 * 以第2行做为标题行，以第3行做为内容起始行
	 * 
	 * @param clazz 模型类信息
	 * @param absoluteFileName 绝对路径名
	 */
	public static HashMap<Object, Object> loadModel(Class<?> clazz, String absoluteFileName) {
		return loadModel(clazz, absoluteFileName, null, 1, 2, 0);
	}
	
	/**
	 * 从指定名称的Sheet页面加载模型配置文件，按照传递的类信息创建映射到Excel内容的对象，
	 * 根据标题行索引获取标题（标题内容映射到对象的属性名），从内容起始行索引开始获取分析内容，
	 * 以主键列的内容做为返回结果的键
	 * 
	 * @param clazz 模型类信息
	 * @param absoluteFileName 绝对路径名
	 * @param sheetName Sheet页名，如果为null则获取第一个Sheet页
	 * @param titleRowIndex 标题行索引，标题行用来与模型对象中的字段建立映射
	 * @param contentRowIndex 内容起始行所在Excel中的索引（从0开始）
	 * @param keyColumnIndex 做为主键的列所在Excel中的索引（从0开始）
	 */
	public static HashMap<Object, Object> loadModel(Class<?> clazz, String absoluteFileName, String sheetName, 
			int titleRowIndex, int contentRowIndex, int keyColumnIndex) {
		/**
		 * 变量定义
		 */
		// 返回的配置文件映射
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		// 每条记录
		Object item = null;
		// 标题行
		XSSFRow titleRow = null;
		
		// 每条Excel记录所映射对象的所有属性中的一个
		Field field = null;
		
		/**
		 * 通过绝对路径名载入Excel文件
		 */
		File file = new File(absoluteFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		/**
		 * 将Excel文件转换为Excel对象
		 */
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
		 * 根据Sheet页名从Excel对象中获取Sheet页并解析
		 * 如果Sheet页名为空，则获取第一个Sheet页
		 */
		XSSFSheet sheet = null;
		if (sheetName == null) {
			sheet = workbook.getSheetAt(0);
		} else {
			sheet = workbook.getSheet(sheetName);
		}
		
		// 为标题行赋值
		titleRow = sheet.getRow(titleRowIndex);
		
		/**
		 *  从内容起始行开始迭代全部内容
		 *  （其中，sheet.getLastRowNum()返回的是最后一行的索引，
		 *  因此在循环的终止条件中要包含该索引值才能获取到最后一行的内容）
		 */
		for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			
			// 如果遇到空行，跳过
			if (row == null) {
				continue;
			}
			
			// 每当有一行Excel内容，创建一个对象的新实例对应它
			try {
				item = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
			/**
			 *  迭代所有列
			 *  （row.getLastCellNum()返回的是列数，与getLastRowNum()
			 *  计算方式不同，因此循环终止条件中不包含该值）
			 */
			for (int j = 0; j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				
				// 如果遇到空列，跳过
				if (cell == null) {
					continue;
				}
				
				// 获取列标题，然后根据列标题即映射的对象属性名获取对象属性
				try {
					XSSFCell cellTemp = titleRow.getCell(j);
					if (cellTemp == null) {
						continue;
					}
					
					field = clazz.getDeclaredField(cellTemp.getStringCellValue());
				} catch (NoSuchFieldException | SecurityException | NullPointerException e) {
					// 如果找不到对应的字段，
					// 是在Excel中存在该列但在对象中无对应的属性，属于正常情况
					continue;
				}
				
				// 根据Excel单元格为对象的属性赋值
				setValue(field, cell, item);
			}
			
			try {
				// 根据指定的主键列索引，在标题行获取该列的标题名，再根据此标题名获取指定对象映射属性的值
				Object key = getValue(item.getClass().getDeclaredField(titleRow.getCell(keyColumnIndex).getStringCellValue()), item);
				if (key != null) {
					// 将解析的Excel行映射后的对象，保存的返回值中
					data.put(key, item);
				}
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	/**
	 * 加载模型配置文件，从第1个Sheet页
	 * 以第2行做为标题行，以第3行做为内容起始行
	 * 返回结果的ArrayList
	 * 
	 * @param clazz 模型类信息
	 * @param absoluteFileName 绝对路径名
	 */
	public static ArrayList<Object> loadArrayListModel(Class<?> clazz, String absoluteFileName) {
		return loadArrayListModel(clazz, absoluteFileName, null, 1, 2, 0);
	}
	
	/**
	 * 从指定名称的Sheet页面加载模型配置文件，按照传递的类信息创建映射到Excel内容的对象，
	 * 根据标题行索引获取标题（标题内容映射到对象的属性名），从内容起始行索引开始获取分析内容，
	 * 返回结果的ArrayList
	 * 
	 * @param clazz 模型类信息
	 * @param absoluteFileName 绝对路径名
	 * @param sheetName Sheet页名，如果为null则获取第一个Sheet页
	 * @param titleRowIndex 标题行索引，标题行用来与模型对象中的字段建立映射
	 * @param contentRowIndex 内容起始行所在Excel中的索引（从0开始）
	 * @param keyColumnIndex 做为主键的列所在Excel中的索引（从0开始）
	 */
	public static ArrayList<Object> loadArrayListModel(Class<?> clazz, String absoluteFileName, String sheetName, 
			int titleRowIndex, int contentRowIndex, int keyColumnIndex) {
		
		ArrayList<Object> data = new ArrayList<>();
		// 每条记录
		Object item = null;
		// 标题行
		XSSFRow titleRow = null;
		
		// 每条Excel记录所映射对象的所有属性中的一个
		Field field = null;
		
		/**
		 * 通过绝对路径名载入Excel文件
		 */
		File file = new File(absoluteFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		/**
		 * 将Excel文件转换为Excel对象
		 */
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
		 * 根据Sheet页名从Excel对象中获取Sheet页并解析
		 * 如果Sheet页名为空，则获取第一个Sheet页
		 */
		XSSFSheet sheet = null;
		if (sheetName == null) {
			sheet = workbook.getSheetAt(0);
		} else {
			sheet = workbook.getSheet(sheetName);
		}
		
		// 为标题行赋值
		titleRow = sheet.getRow(titleRowIndex);
		
		/**
		 *  从内容起始行开始迭代全部内容
		 *  （其中，sheet.getLastRowNum()返回的是最后一行的索引，
		 *  因此在循环的终止条件中要包含该索引值才能获取到最后一行的内容）
		 */
		for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			
			// 如果遇到空行，跳过
			if (row == null) {
				continue;
			}
			
			// 每当有一行Excel内容，创建一个对象的新实例对应它
			try {
				item = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
			/**
			 *  迭代所有列
			 *  （row.getLastCellNum()返回的是列数，与getLastRowNum()
			 *  计算方式不同，因此循环终止条件中不包含该值）
			 */
			for (int j = 0; j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				
				// 如果遇到空列，跳过
				if (cell == null) {
					continue;
				}
				
				// 获取列标题，然后根据列标题即映射的对象属性名获取对象属性
				try {
					field = clazz.getDeclaredField(titleRow.getCell(j).getStringCellValue());
				} catch (NoSuchFieldException | SecurityException | NullPointerException e) {
					// 如果找不到对应的字段，
					// 是在Excel中存在该列但在对象中无对应的属性，属于正常情况
					continue;
				}
				
				// 根据Excel单元格为对象的属性赋值
				setValue(field, cell, item);
			}
			data.add(item);
		}
		return data;
	}
	
	/**
	 * 获取对象指定属性的值
	 * @param field 待获取的对象属性
	 * @param item 对象
	 * @return 获取到的对象属性的内容
	 */
	private static Object getValue(Field field, Object item) {
		// 获取字段访问修饰符
		int modifiers = field.getModifiers();
		
		if (Modifier.isPublic(modifiers)) {
			try {
				return field.get(item);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			/*
			 *  获取字段名并将字段名首字母大写，
			 *  以满足get/set访问器使用驼峰表示法后，
			 *  get/set后面拼接的字段名首字母大写
			 */
			char[] charArray = field.getName().toCharArray();
			if (charArray[0] >= 97 && charArray[0] <= 122) {
				charArray[0] -= 32;
			}
			
			try {
				Method getMethod = item.getClass().getMethod("get" + String.valueOf(charArray));
				try {
					return getMethod.invoke(item);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * 根据对象属性访问修饰符的类型，决定是通过Field直接修改属性内容，
	 * 还是通过get/set访问器通过Method.invoke修改属性内容。
	 * 
	 * 在修改属性内容时，根据Excel的单元格类型（Numeric/Text）和属性类型（int/String/float/double），
	 * 在一系列类型转换之后，完成赋值。
	 * 
	 * 方法内对于对象和对象属性的修改在方法外生效，不需要返回值
	 * 
	 * @param field 待赋值的对象属性
	 * @param cell 存有值的Excel单元格
	 * @param item 属性归属的对象
	 */
	private static void setValue(Field field, XSSFCell cell, Object item) {
		// 获取字段访问修饰符
		int modifiers = field.getModifiers();
		// 如果是公有字段
		if (Modifier.isPublic(modifiers)) {
			try {
				if (field.getType() == int.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						field.set(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							field.set(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0);
						}
						break;
					}
				} else if (field.getType() == String.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						field.set(item, String.valueOf(cell.getNumericCellValue()));
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						field.set(item, cell.getStringCellValue());
						break;
					}
				} else if (field.getType() == double.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						field.set(item, cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							field.set(item, Double.parseDouble(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0d);
						}
						break;
					}
				} else if (field.getType() == float.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						field.set(item, (float)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							field.set(item, Float.parseFloat(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0f);
						}
						break;
					}
				} else if (field.getType() == long.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						field.set(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							field.set(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0);
						}
						break;
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		// 如果是私有或者其他类型字段
		} else {
			/*
			 *  获取字段名并将字段名首字母大写，
			 *  以满足get/set访问器使用驼峰表示法后，
			 *  get/set后面拼接的字段名首字母大写
			 */
			char[] charArray = field.getName().toCharArray();
			if (charArray[0] >= 97 && charArray[0] <= 122) {
				charArray[0] -= 32;
			}
			
			try {
				Method setMethod = item.getClass().getMethod("set" + String.valueOf(charArray), field.getType());
				if (field.getType() == int.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						setMethod.invoke(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							setMethod.invoke(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0);
						}
						break;
					}
				} else if (field.getType() == String.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						setMethod.invoke(item, String.valueOf(cell.getNumericCellValue()));
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						setMethod.invoke(item, cell.getStringCellValue());
						break;
					}
				} else if (field.getType() == double.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						setMethod.invoke(item, cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							setMethod.invoke(item, Double.parseDouble(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0d);
						}
						break;
					}
				} else if (field.getType() == float.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						setMethod.invoke(item, (float)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							setMethod.invoke(item, Float.parseFloat(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0f);
						}
						break;
					}
				} else if (field.getType() == long.class) {
					// 根据Excel单元格的类型获取单元格内容
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // 数字型单元格内容
						setMethod.invoke(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // 文本型单元格内容
					default: // 其他类型单元格内容
						try {
							setMethod.invoke(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0);
						}
						break;
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}