import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作shp文件的操作类 
 * @author Walt.Chiang
 * 2016年1月14日
 */
public class ShapeOprate {
	/**
	 * 新建一shp 文件，并将后续的 字段以及值写入到文件中。生成shp文件坐标系默认是wgs84
	 * 对于 geometry类型（shape）必须字段名称必须用 the_geom,字段类型可以是Point.class、LineString.class、Polygon.class等
	 * 要求dataRows的每一行list的长度与fields的长度相等
	 * @param filepath shp文 件路径
	 * @param fields   字段信息
	 * @param datas    数据
	 */
	public void write(String filepath,List<FieldEntity> fields,List<List<Object>> dataRows) {
		if(filepath==null||fields==null||dataRows==null)
			return;
		if(dataRows.size()==0){
			
		}
		else if(fields.size()!=dataRows.get(0).size()){
			return;
		}
		try {
			//创建shape文件对象
			File file = new File(filepath);
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
			//定义图形信息和属性信息
			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
			tb.setCRS(DefaultGeographicCRS.WGS84);
			tb.setName("shapefile");

			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
			WKTReader wtkreader = new WKTReader( geometryFactory );   
			 tb.setDefaultGeometry("the_geom");
			for (int i=0;i<fields.size();i++) {
				
				if(fields.get(i).fieldName.equals("the_geom")){
                  
					tb.add(fields.get(i).fieldName, fields.get(i).fieldType,DefaultGeographicCRS.WGS84);//设置字段
				}
				else{
					tb.add(fields.get(i).fieldName, fields.get(i).fieldType);//设置字段
				}
			}
			SimpleFeatureType sft= tb.buildFeatureType();
			ds.createSchema(sft);
			ds.setCharset(Charset.forName("utf-8"));
			
			//设置Writer
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
			//写下一条
			//遍历写入数据			
			for(int index=0;index<dataRows.size();index++){
				List<Object> row=dataRows.get(index);
				if(row!=null&&fields.size()==row.size()){
					SimpleFeature feature = writer.next();
					for(int fIndex=0;fIndex<row.size();fIndex++){
						if(row.get(fIndex).getClass()== (fields.get(fIndex).fieldType)){
							feature.setAttribute(fields.get(fIndex).fieldName, row.get(fIndex));
						}
						else if(fields.get(fIndex).fieldName.equals("the_geom")&&row.get(fIndex).getClass()==String.class){
							
							   String wkt=row.get(fIndex).toString();
						        Geometry  geom =  wtkreader.read(wkt);
						        if( fields.get(fIndex).fieldType==LineString.class){
						        	feature.setAttribute(fields.get(fIndex).fieldName, (LineString)geom);
						        }
						        else  if( fields.get(fIndex).fieldType==Polygon.class){
						        	feature.setAttribute(fields.get(fIndex).fieldName, (Polygon)geom);
						        }
						        else  if( fields.get(fIndex).fieldType==Point.class){
						        	feature.setAttribute(fields.get(fIndex).fieldName, (Point)geom);
						        }
						
						}
					}
				}
				
				
			}
			writer.write();
			writer.close();
			ds.dispose();
			
			//读取刚写完shape文件的图形信息
			ShpFiles shpFiles = new ShpFiles(filepath);
			ShapefileReader reader = new ShapefileReader(shpFiles, false, true, new GeometryFactory(), false);
			try {
				while (reader.hasNext()) {
					System.out.println(reader.nextRecord().shape());	
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {e.printStackTrace();  	}
	}
	/**
	 * 读取一个shape 文件返回geometry
	 * @param shpPath shape 文件路径
	 * @return 
	 */
	public List<Geometry> readShp(String shpPath){
		List<Geometry> lists=new ArrayList<Geometry>();
		try {  
		    ShpFiles sf = new ShpFiles(shpPath);
		    ShapefileReader r = new ShapefileReader( sf, false, false, new GeometryFactory() );
		    while (r.hasNext()) {  
		        Geometry shape = (Geometry) r.nextRecord().shape();  //com.vividsolutions.jts.geom.Geometry;  
		        lists.add(shape);
		    }   
		    r.close();  
		    return lists;
		} catch (Exception e) {  
		    e.printStackTrace();  
		}  
		return null;
	}
	/**
	 * 读取dbf内容以及list返回。
	 * @param dbfPath dbf路基
	 * @return
	 */
	public List<Map> readDBF(String dbfPath) {
		List<Map> results=new ArrayList<Map>();
		try {
			FileChannel in = new FileInputStream(dbfPath).getChannel();
			Charset cs = Charset.forName("GBK");
//			if("UTF-8".equalsIgnoreCase(cs.name())){
//				cs = Charset.forName("GBK");
//			}
			DbaseFileReader dbfReader =  new DbaseFileReader(in, false,  cs);
			DbaseFileHeader header = dbfReader.getHeader();
			int fields = header.getNumFields();
			
			
			while ( dbfReader.hasNext() ){
				DbaseFileReader.Row row =  dbfReader.readRow();
//				System.out.println(row.toString());
				Map item=new HashMap();
				for (int i=0; i<fields; i++) {					
					item.put(header.getFieldName(i), row.read(i));
				}
				results.add(item);
			}
			dbfReader.close();
			in.close();
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 由源shape文件创建新的shape文件
	 * @param srcfilepath 源shape文件
	 * @param destfilepath 目标shape文件
	 */
	public void transShape(String srcfilepath, String destfilepath) {
		if(srcfilepath==null||destfilepath==null){
			return;
		}
		try {
			//源shape文件
			ShapefileDataStore shapeDS = (ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(new File(srcfilepath).toURI().toURL());
			//创建目标shape文件对象
			Map<String, Serializable> params = new HashMap<String, Serializable>();
	        FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();
	        params.put(ShapefileDataStoreFactory.URLP.key, new File(destfilepath).toURI().toURL());
	        ShapefileDataStore ds = (ShapefileDataStore) factory.createNewDataStore(params);
	        // 设置属性
	        SimpleFeatureSource fs = shapeDS.getFeatureSource(shapeDS.getTypeNames()[0]);
	        //下面这行还有其他写法，根据源shape文件的simpleFeatureType可以不用retype，而直接用fs.getSchema设置
	        ds.createSchema(SimpleFeatureTypeBuilder.retype(fs.getSchema(), DefaultGeographicCRS.WGS84));
	        
	        //设置writer
	        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
	        
	        //写记录
	        SimpleFeatureIterator it = fs.getFeatures().features();
	        try {
	            while (it.hasNext()) {
	                SimpleFeature f = it.next();
	                SimpleFeature fNew = writer.next();
	                fNew.setAttributes(f.getAttributes());
	                writer.write();
	            }
	        } finally {
	            it.close();
	        }
	        writer.close();
	        ds.dispose();
	        shapeDS.dispose();
		} catch (Exception e) { e.printStackTrace();	}
	}  
}

