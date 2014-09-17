package ti.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import ti.map.MapModule;
import ti.map.Shape.IShape;
import ti.map.Shape.Shape;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

@Kroll.proxy(name = "Polygon", creatableInModule = MapModule.class, propertyAccessors = {

PolygonProxy.PROPERTY_FILL_COLOR, PolygonProxy.PROPERTY_STROKE_COLOR,
		PolygonProxy.PROPERTY_STROKE_WIDTH, PolygonProxy.PROPERTY_ZINDEX,
		MapModule.PROPERTY_POINTS, PolygonProxy.PROPERTY_HOLES,

// TiC.PROPERTY_COLOR,
// TiC.PROPERTY_WIDTH
})
public class PolygonProxy extends KrollProxy implements IShape {

	private PolygonOptions options;
	private Polygon polygon;

	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

	// int MSG_SET_POINTS
	// int MSG_SET_FILL_COLOR
	// int MSG_SET_STROKE_COLOR
	// int MSG_SET_STROKE_WIDTH
	// int MSG_SET_ZINDEX
	private static final int MSG_SET_POINTS = MSG_FIRST_ID + 699;
	private static final int MSG_SET_FILL_COLOR = MSG_FIRST_ID + 700;
	private static final int MSG_SET_STROKE_COLOR = MSG_FIRST_ID + 701;
	private static final int MSG_SET_STROKE_WIDTH = MSG_FIRST_ID + 702;
	private static final int MSG_SET_ZINDEX = MSG_FIRST_ID + 703;
	private static final int MSG_SET_HOLES = MSG_FIRST_ID + 704;

	// points (MapView)
	// (int) strokeColor
	// (float) strokeWidth
	// (int) fillColor
	// (float) zIndex
	public static final String PROPERTY_STROKE_COLOR = "strokeColor";
	public static final String PROPERTY_STROKE_WIDTH = "strokeWidth";
	public static final String PROPERTY_FILL_COLOR = "fillColor";
	public static final String PROPERTY_ZINDEX = "zIndex";
	public static final String PROPERTY_HOLES = "holes";

	public PolygonProxy() {
		super();
	}

	public PolygonProxy(TiContext tiContext) {
		this();
	}

	@Override
	public boolean handleMessage(Message msg) {

		// MSG_SET_POINTS
		// MSG_SET_STROKE_WIDTH
		// MSG_SET_STROKE_COLOR
		// MSG_SET_FILL_COLOR
		// MSG_SET_ZINDEX
		AsyncResult result = null;
		switch (msg.what) {
		case MSG_SET_POINTS: {
			result = (AsyncResult) msg.obj;
			// Log.e("PolygonProxy.handleMessage.MSG_SET_POINTS",
			// result.getArg().toString());
			polygon.setPoints(processPoints(result.getArg(), true));
			result.setResult(null);
			return true;
		}
		case MSG_SET_HOLES: {
			result = (AsyncResult) msg.obj;
			polygon.setHoles(processHoles(result.getArg(), true));
			result.setResult(null);
			return true;
		}
		case MSG_SET_FILL_COLOR: {
			result = (AsyncResult) msg.obj;
			options.fillColor((Integer) result.getArg());
			result.setResult(null);
			return true;
		}
		case MSG_SET_STROKE_WIDTH: {
			result = (AsyncResult) msg.obj;
			options.strokeWidth((Float) result.getArg());
			result.setResult(null);
			return true;
		}
		case MSG_SET_STROKE_COLOR: {
			result = (AsyncResult) msg.obj;
			options.strokeColor((Integer) result.getArg());
			result.setResult(null);
			return true;
		}
		case MSG_SET_ZINDEX: {
			result = (AsyncResult) msg.obj;
			options.zIndex((Float) result.getArg());
			result.setResult(null);
			return true;
		}
		default: {
			return super.handleMessage(msg);
		}
		}
	}

	public void processOptions() {

		options = new PolygonOptions();
		String op;
		// (int) strokeColor
		// (float) strokeWidth
		// (int) fillColor
		// (float) zIndex

		if (hasProperty(MapModule.PROPERTY_POINTS)) {
			processPoints(getProperty(MapModule.PROPERTY_POINTS), false);
		}

		if (hasProperty(PolygonProxy.PROPERTY_HOLES)) {
			processHoles(getProperty(PolygonProxy.PROPERTY_HOLES), false);
		}

		op = PolygonProxy.PROPERTY_STROKE_COLOR;
		if (hasProperty(op)) {
			options.strokeColor(TiConvert.toColor((String) getProperty(op)));
		}

		op = PolygonProxy.PROPERTY_STROKE_WIDTH;
		if (hasProperty(op)) {
			options.strokeWidth(TiConvert.toFloat(getProperty(op)));
		}

		op = PolygonProxy.PROPERTY_FILL_COLOR;
		if (hasProperty(op)) {
			options.fillColor(TiConvert.toColor((String) getProperty(op)));
		}

		op = PolygonProxy.PROPERTY_ZINDEX;
		if (hasProperty(op)) {
			options.zIndex(TiConvert.toFloat(getProperty(op)));
		}

	}

	public void addLocation(Object loc, ArrayList<LatLng> locationArray, boolean list) {
		LatLng location = parseLocation(loc);
		if (list) {
			locationArray.add(location);
		} else {
			options.add(location);
		}
	}

	public ArrayList<LatLng> processPoints(Object points, boolean list) {

		ArrayList<LatLng> locationArray = new ArrayList<LatLng>();
		// multiple points
		if (points instanceof Object[]) {
			Object[] pointsArray = (Object[]) points;		
			for (int i = 0; i < pointsArray.length; i++) {
				Object obj = pointsArray[i];
				addLocation(obj, locationArray, list);
			}
			return locationArray;
		}

		// single point
		addLocation(points, locationArray, list);
		return locationArray;
	}

	/**
	 * Add holes as a list of list
	 * 
	 * holes: [ [ { latitude: .., longitude: .. } ] ]
	 * 
	 */
	public ArrayList<ArrayList<LatLng>> processHoles(Object holesList,
			boolean list) {

		ArrayList<ArrayList<LatLng>> holesArray = new ArrayList<ArrayList<LatLng>>();

		// multiple points
		if (holesList instanceof Object[]) {

			Object[] singleHoleArray = (Object[]) holesList;
			for (int h = 0; h < singleHoleArray.length; h++) {
				
				ArrayList<LatLng> holeContainerArray = new ArrayList<LatLng>();
				
				Object[] pointsArray = (Object[]) singleHoleArray[h];
				if (pointsArray instanceof Object[]) {
					for (int i = 0; i < pointsArray.length; i++) {
						Object obj = pointsArray[i];
						
//						if (obj instanceof HashMap) {
//							HashMap<String, String> point = (HashMap<String, String>) obj;
//							LatLng location = new LatLng(
//									TiConvert.toDouble(point
//											.get(TiC.PROPERTY_LATITUDE)),
//									TiConvert.toDouble(point
//											.get(TiC.PROPERTY_LONGITUDE)));
//							holeContainerArray.add(location);
//						}

						holeContainerArray.add(parseLocation(obj));
						
					}
				}	
				
				if (holeContainerArray.size() > 0) {
					if (!list) {
						if (polygon == null) {
							// Log.e("TiApplicationMapDBG",
							// "add holes to options");
							options.addHole(holeContainerArray);
						}
					} else {
						holesArray.add(holeContainerArray);
					}
				}
			}
		}

		if (!list) {
			// Log.e("TiApplicationMapDBG", "polygon exists?");
			if (polygon != null) {
				// Log.e("TiApplicationMapDBG", "Yes, add holes to polygon");
				polygon.setHoles(holesArray);
			}

			return null;
		} else
			return holesArray;

	}

	public PolygonOptions getOptions() {
		return options;
	}

	@Kroll.method
	public void setHoles(Object[] holesList) {
		TiMessenger.sendBlockingMainMessage(
				getMainHandler().obtainMessage(MSG_SET_HOLES), holesList);
	}

	public void setPolygon(Polygon r) {
		polygon = r;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public List<? extends List<LatLng>> getHoles() {
		return polygon.getHoles();
	}

	@Override
	public void onPropertyChanged(String name, Object value) {

		super.onPropertyChanged(name, value);

		if (polygon == null) {
			return;
		}

		else if (name.equals(MapModule.PROPERTY_POINTS)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_POINTS), value);
		} else if (name.equals(PolygonProxy.PROPERTY_HOLES)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_HOLES), value);
		} else if (name.equals(PolygonProxy.PROPERTY_STROKE_WIDTH)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_STROKE_WIDTH),
					TiConvert.toFloat(value));
		}

		else if (name.equals(PolygonProxy.PROPERTY_STROKE_COLOR)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_STROKE_COLOR),
					TiConvert.toColor((String) value));
		}

		else if (name.equals(PolygonProxy.PROPERTY_FILL_COLOR)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_FILL_COLOR),
					TiConvert.toColor((String) value));
		}

		else if (name.equals(PolygonProxy.PROPERTY_ZINDEX)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_ZINDEX),
					TiConvert.toFloat(value));
		}

	}

	protected TiMarker marker;

	/**
	 * Handle timarker reference
	 * */
	public void setTiMarker(TiMarker marker) {
		this.marker = marker;
	}

	public TiMarker getTiMarker() {
		return this.marker;
	}

	public Marker getMarker() {
		return this.marker != null ? this.marker.getMarker() : null;
	}

	public AnnotationProxy getAnnotation() {
		return this.marker != null ? this.marker.getProxy() : null;
	}

	// A location can either be a an array of longitude, latitude pairings or
	// an array of longitude, latitude objects.
	// e.g. [123.33, 34.44], OR {longitude: 123.33, latitude, 34.44}
	private LatLng parseLocation(Object loc) {
		LatLng location = null;
		if (loc instanceof HashMap) {
			HashMap<String, String> point = (HashMap<String, String>) loc;
			location = new LatLng(TiConvert.toDouble(point
					.get(TiC.PROPERTY_LATITUDE)), TiConvert.toDouble(point
					.get(TiC.PROPERTY_LONGITUDE)));
		} else if (loc instanceof Object[]) {
			Object[] temp = (Object[]) loc;
			location = new LatLng(TiConvert.toDouble(temp[1]), TiConvert.toDouble(temp[0]));
		}
		return location;
	}

}
