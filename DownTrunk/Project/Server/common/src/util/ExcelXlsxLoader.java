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
 * �����Excelһ����¼��ӳ��
 * 
 * ע�⣺
 * 1����֪�ܻ�ȡ���ֺ��ı����͵�Ԫ������ݣ��������͵�Ԫ�������ֻ�ܳ��Զ�ȡ�ı�����
 * 2����ӳ����������Ҫô�ǹ��еģ�Ҫô�Ƿǹ��е�������ṩ��׼��get/set������
 * 3��Excel�еı����б������ı��͵ĵ�Ԫ��
 * 4�����Excel��Ԫ��������������͵ģ���ӳ��Ķ����������ı����͵ģ���ô��ȡ�������ݿ��ܲ�ƥ�䡣
 *    ���磺Excel��Ԫ���ʽNumeric������12345����ֵ�����������е�������"12345.0"
 * 5���������String���͵ģ������������ͽ��л�ȡ���ǻ�ȡ�������ݵġ�
 *    ������ΪObject�����µ���ʽת������������intת��ΪInteger,��String����һ������
 * 6����֪֧�ְ汾Ϊ2007 ~ 2010��׺Ϊ.xlsx��ʽ��Excel
 * 
 */
public class ExcelXlsxLoader {
	
	/**
	 * ����ģ�������ļ����ӵ�1��Sheetҳ���Ե�1����Ϊ������
	 * �Ե�2����Ϊ�����У��Ե�3����Ϊ������ʼ��
	 * 
	 * @param clazz ģ������Ϣ
	 * @param absoluteFileName ����·����
	 */
	public static HashMap<Object, Object> loadModel(Class<?> clazz, String absoluteFileName) {
		return loadModel(clazz, absoluteFileName, null, 1, 2, 0);
	}
	
	/**
	 * ��ָ�����Ƶ�Sheetҳ�����ģ�������ļ������մ��ݵ�����Ϣ����ӳ�䵽Excel���ݵĶ���
	 * ���ݱ�����������ȡ���⣨��������ӳ�䵽�����������������������ʼ��������ʼ��ȡ�������ݣ�
	 * �������е�������Ϊ���ؽ���ļ�
	 * 
	 * @param clazz ģ������Ϣ
	 * @param absoluteFileName ����·����
	 * @param sheetName Sheetҳ�������Ϊnull���ȡ��һ��Sheetҳ
	 * @param titleRowIndex ������������������������ģ�Ͷ����е��ֶν���ӳ��
	 * @param contentRowIndex ������ʼ������Excel�е���������0��ʼ��
	 * @param keyColumnIndex ��Ϊ������������Excel�е���������0��ʼ��
	 */
	public static HashMap<Object, Object> loadModel(Class<?> clazz, String absoluteFileName, String sheetName, 
			int titleRowIndex, int contentRowIndex, int keyColumnIndex) {
		/**
		 * ��������
		 */
		// ���ص������ļ�ӳ��
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		// ÿ����¼
		Object item = null;
		// ������
		XSSFRow titleRow = null;
		
		// ÿ��Excel��¼��ӳ���������������е�һ��
		Field field = null;
		
		/**
		 * ͨ������·��������Excel�ļ�
		 */
		File file = new File(absoluteFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		/**
		 * ��Excel�ļ�ת��ΪExcel����
		 */
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
		 * ����Sheetҳ����Excel�����л�ȡSheetҳ������
		 * ���Sheetҳ��Ϊ�գ����ȡ��һ��Sheetҳ
		 */
		XSSFSheet sheet = null;
		if (sheetName == null) {
			sheet = workbook.getSheetAt(0);
		} else {
			sheet = workbook.getSheet(sheetName);
		}
		
		// Ϊ�����и�ֵ
		titleRow = sheet.getRow(titleRowIndex);
		
		/**
		 *  ��������ʼ�п�ʼ����ȫ������
		 *  �����У�sheet.getLastRowNum()���ص������һ�е�������
		 *  �����ѭ������ֹ������Ҫ����������ֵ���ܻ�ȡ�����һ�е����ݣ�
		 */
		for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			
			// ����������У�����
			if (row == null) {
				continue;
			}
			
			// ÿ����һ��Excel���ݣ�����һ���������ʵ����Ӧ��
			try {
				item = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
			/**
			 *  ����������
			 *  ��row.getLastCellNum()���ص�����������getLastRowNum()
			 *  ���㷽ʽ��ͬ�����ѭ����ֹ�����в�������ֵ��
			 */
			for (int j = 0; j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				
				// ����������У�����
				if (cell == null) {
					continue;
				}
				
				// ��ȡ�б��⣬Ȼ������б��⼴ӳ��Ķ�����������ȡ��������
				try {
					XSSFCell cellTemp = titleRow.getCell(j);
					if (cellTemp == null) {
						continue;
					}
					
					field = clazz.getDeclaredField(cellTemp.getStringCellValue());
				} catch (NoSuchFieldException | SecurityException | NullPointerException e) {
					// ����Ҳ�����Ӧ���ֶΣ�
					// ����Excel�д��ڸ��е��ڶ������޶�Ӧ�����ԣ������������
					continue;
				}
				
				// ����Excel��Ԫ��Ϊ��������Ը�ֵ
				setValue(field, cell, item);
			}
			
			try {
				// ����ָ�����������������ڱ����л�ȡ���еı��������ٸ��ݴ˱�������ȡָ������ӳ�����Ե�ֵ
				Object key = getValue(item.getClass().getDeclaredField(titleRow.getCell(keyColumnIndex).getStringCellValue()), item);
				if (key != null) {
					// ��������Excel��ӳ���Ķ��󣬱���ķ���ֵ��
					data.put(key, item);
				}
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	/**
	 * ����ģ�������ļ����ӵ�1��Sheetҳ
	 * �Ե�2����Ϊ�����У��Ե�3����Ϊ������ʼ��
	 * ���ؽ����ArrayList
	 * 
	 * @param clazz ģ������Ϣ
	 * @param absoluteFileName ����·����
	 */
	public static ArrayList<Object> loadArrayListModel(Class<?> clazz, String absoluteFileName) {
		return loadArrayListModel(clazz, absoluteFileName, null, 1, 2, 0);
	}
	
	/**
	 * ��ָ�����Ƶ�Sheetҳ�����ģ�������ļ������մ��ݵ�����Ϣ����ӳ�䵽Excel���ݵĶ���
	 * ���ݱ�����������ȡ���⣨��������ӳ�䵽�����������������������ʼ��������ʼ��ȡ�������ݣ�
	 * ���ؽ����ArrayList
	 * 
	 * @param clazz ģ������Ϣ
	 * @param absoluteFileName ����·����
	 * @param sheetName Sheetҳ�������Ϊnull���ȡ��һ��Sheetҳ
	 * @param titleRowIndex ������������������������ģ�Ͷ����е��ֶν���ӳ��
	 * @param contentRowIndex ������ʼ������Excel�е���������0��ʼ��
	 * @param keyColumnIndex ��Ϊ������������Excel�е���������0��ʼ��
	 */
	public static ArrayList<Object> loadArrayListModel(Class<?> clazz, String absoluteFileName, String sheetName, 
			int titleRowIndex, int contentRowIndex, int keyColumnIndex) {
		
		ArrayList<Object> data = new ArrayList<>();
		// ÿ����¼
		Object item = null;
		// ������
		XSSFRow titleRow = null;
		
		// ÿ��Excel��¼��ӳ���������������е�һ��
		Field field = null;
		
		/**
		 * ͨ������·��������Excel�ļ�
		 */
		File file = new File(absoluteFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		/**
		 * ��Excel�ļ�ת��ΪExcel����
		 */
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
		 * ����Sheetҳ����Excel�����л�ȡSheetҳ������
		 * ���Sheetҳ��Ϊ�գ����ȡ��һ��Sheetҳ
		 */
		XSSFSheet sheet = null;
		if (sheetName == null) {
			sheet = workbook.getSheetAt(0);
		} else {
			sheet = workbook.getSheet(sheetName);
		}
		
		// Ϊ�����и�ֵ
		titleRow = sheet.getRow(titleRowIndex);
		
		/**
		 *  ��������ʼ�п�ʼ����ȫ������
		 *  �����У�sheet.getLastRowNum()���ص������һ�е�������
		 *  �����ѭ������ֹ������Ҫ����������ֵ���ܻ�ȡ�����һ�е����ݣ�
		 */
		for (int i = contentRowIndex; i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			
			// ����������У�����
			if (row == null) {
				continue;
			}
			
			// ÿ����һ��Excel���ݣ�����һ���������ʵ����Ӧ��
			try {
				item = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
			/**
			 *  ����������
			 *  ��row.getLastCellNum()���ص�����������getLastRowNum()
			 *  ���㷽ʽ��ͬ�����ѭ����ֹ�����в�������ֵ��
			 */
			for (int j = 0; j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				
				// ����������У�����
				if (cell == null) {
					continue;
				}
				
				// ��ȡ�б��⣬Ȼ������б��⼴ӳ��Ķ�����������ȡ��������
				try {
					field = clazz.getDeclaredField(titleRow.getCell(j).getStringCellValue());
				} catch (NoSuchFieldException | SecurityException | NullPointerException e) {
					// ����Ҳ�����Ӧ���ֶΣ�
					// ����Excel�д��ڸ��е��ڶ������޶�Ӧ�����ԣ������������
					continue;
				}
				
				// ����Excel��Ԫ��Ϊ��������Ը�ֵ
				setValue(field, cell, item);
			}
			data.add(item);
		}
		return data;
	}
	
	/**
	 * ��ȡ����ָ�����Ե�ֵ
	 * @param field ����ȡ�Ķ�������
	 * @param item ����
	 * @return ��ȡ���Ķ������Ե�����
	 */
	private static Object getValue(Field field, Object item) {
		// ��ȡ�ֶη������η�
		int modifiers = field.getModifiers();
		
		if (Modifier.isPublic(modifiers)) {
			try {
				return field.get(item);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			/*
			 *  ��ȡ�ֶ��������ֶ�������ĸ��д��
			 *  ������get/set������ʹ���շ��ʾ����
			 *  get/set����ƴ�ӵ��ֶ�������ĸ��д
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
	 * ���ݶ������Է������η������ͣ�������ͨ��Fieldֱ���޸��������ݣ�
	 * ����ͨ��get/set������ͨ��Method.invoke�޸��������ݡ�
	 * 
	 * ���޸���������ʱ������Excel�ĵ�Ԫ�����ͣ�Numeric/Text�����������ͣ�int/String/float/double����
	 * ��һϵ������ת��֮����ɸ�ֵ��
	 * 
	 * �����ڶ��ڶ���Ͷ������Ե��޸��ڷ�������Ч������Ҫ����ֵ
	 * 
	 * @param field ����ֵ�Ķ�������
	 * @param cell ����ֵ��Excel��Ԫ��
	 * @param item ���Թ����Ķ���
	 */
	private static void setValue(Field field, XSSFCell cell, Object item) {
		// ��ȡ�ֶη������η�
		int modifiers = field.getModifiers();
		// ����ǹ����ֶ�
		if (Modifier.isPublic(modifiers)) {
			try {
				if (field.getType() == int.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						field.set(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							field.set(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0);
						}
						break;
					}
				} else if (field.getType() == String.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						field.set(item, String.valueOf(cell.getNumericCellValue()));
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						field.set(item, cell.getStringCellValue());
						break;
					}
				} else if (field.getType() == double.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						field.set(item, cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							field.set(item, Double.parseDouble(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0d);
						}
						break;
					}
				} else if (field.getType() == float.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						field.set(item, (float)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							field.set(item, Float.parseFloat(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							field.set(item, 0f);
						}
						break;
					}
				} else if (field.getType() == long.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						field.set(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
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
		// �����˽�л������������ֶ�
		} else {
			/*
			 *  ��ȡ�ֶ��������ֶ�������ĸ��д��
			 *  ������get/set������ʹ���շ��ʾ����
			 *  get/set����ƴ�ӵ��ֶ�������ĸ��д
			 */
			char[] charArray = field.getName().toCharArray();
			if (charArray[0] >= 97 && charArray[0] <= 122) {
				charArray[0] -= 32;
			}
			
			try {
				Method setMethod = item.getClass().getMethod("set" + String.valueOf(charArray), field.getType());
				if (field.getType() == int.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						setMethod.invoke(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							setMethod.invoke(item, Integer.parseInt(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0);
						}
						break;
					}
				} else if (field.getType() == String.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						setMethod.invoke(item, String.valueOf(cell.getNumericCellValue()));
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						setMethod.invoke(item, cell.getStringCellValue());
						break;
					}
				} else if (field.getType() == double.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						setMethod.invoke(item, cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							setMethod.invoke(item, Double.parseDouble(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0d);
						}
						break;
					}
				} else if (field.getType() == float.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						setMethod.invoke(item, (float)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
						try {
							setMethod.invoke(item, Float.parseFloat(cell.getStringCellValue()));
						} catch (NumberFormatException e) {
							setMethod.invoke(item, 0f);
						}
						break;
					}
				} else if (field.getType() == long.class) {
					// ����Excel��Ԫ������ͻ�ȡ��Ԫ������
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC: // �����͵�Ԫ������
						setMethod.invoke(item, (int)cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_STRING: // �ı��͵�Ԫ������
					default: // �������͵�Ԫ������
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