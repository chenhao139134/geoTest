import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class GeomUtil {

	public CoordinateReferenceSystem webmactor = null;

	public final String WKT_MERCATOR = "PROJCS[\"World_Mercator\"," + "GEOGCS[\"GCS_WGS_1984\"," + "DATUM[\"WGS_1984\","
			+ "SPHEROID[\"WGS_1984\",6378137,298.257223563]]," + "PRIMEM[\"Greenwich\",0],"
			+ "UNIT[\"Degree\",0.017453292519943295]]," + "PROJECTION[\"Mercator_1SP\"],"
			+ "PARAMETER[\"False_Easting\",0]," + "PARAMETER[\"False_Northing\",0],"
			+ "PARAMETER[\"Central_Meridian\",0]," + "PARAMETER[\"latitude_of_origin\",0]," + "UNIT[\"Meter\",1]]";

	private static CoordinateReferenceSystem CRS4490 = null;
	private static CoordinateReferenceSystem CRS4508 = null;

	private static GeomUtil instance = null;

	private GeomUtil() {
		try {
			CRS4490 = CRS.parseWKT(
					"GEOGCS[\"China Geodetic Coordinate System 2000\",DATUM[\"China_2000\",SPHEROID[\"CGCS2000\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"1024\"]],AUTHORITY[\"EPSG\",\"1043\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4490\"]]");
			CRS4508 = CRS.parseWKT(
					"PROJCS[\"CGCS2000 / Gauss-Kruger CM 111E\",GEOGCS[\"China Geodetic Coordinate System 2000\",DATUM[\"China_2000\",SPHEROID[\"CGCS2000\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"1024\"]],AUTHORITY[\"EPSG\",\"1043\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4490\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",111],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AUTHORITY[\"EPSG\",\"4508\"]]");
		} catch (FactoryException e) {
			e.printStackTrace();
		}
	}

	public static synchronized GeomUtil getInstance() {
		if (null == instance) {
			instance = new GeomUtil();
		}
		return instance;
	}

	public Geometry lonlat2WebMactor(Geometry geom) {
		try {
			// 投影转换
			MathTransform transform = CRS.findMathTransform(CRS4490, CRS4508);
			return JTS.transform(geom, transform);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Geometry webMactor2Lonlat(Geometry geom) {
		try {
			// 投影转换
			MathTransform transform = CRS.findMathTransform(CRS4508, CRS4490);
			return JTS.transform(geom, transform);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CoordinateReferenceSystem getCRS4490() {
		return CRS4490;
	}

	public static CoordinateReferenceSystem getCRS4508() {
		return CRS4508;
	}
	

}
