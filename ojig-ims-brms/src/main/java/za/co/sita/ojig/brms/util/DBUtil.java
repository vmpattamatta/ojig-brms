package za.co.sita.ojig.brms.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.sita.ojig.ims.hib.bean.GeoLoc;
import za.co.sita.ojig.ims.hib.bean.Incident;
import za.co.sita.ojig.ims.hib.bean.Status;
import za.co.sita.ojig.ims.hib.bean.Supplier;
import za.co.sita.ojig.ims.hib.dao.GeoLocHome;
import za.co.sita.ojig.ims.hib.dao.IncSuppHome;
import za.co.sita.ojig.ims.hib.dao.IncidentHome;
import za.co.sita.ojig.ims.hib.dao.StatusHome;
import za.co.sita.ojig.ims.hib.dao.SupplierHome;

public class DBUtil implements Serializable {
	private static final long serialVersionUID = -6497626553977369449L;
	private IncidentHome incidentHome;
	private StatusHome statusHome;
	private GeoLocHome geoLocHome;
	private IncSuppHome incSuppHome;
	private SupplierHome supplierHome;

	public void updateIncident(Incident incident) {
		incidentHome.merge(incident);
	}

	public List<Supplier> getAllSuppliers() {
		return supplierHome.findAll();
	}

	public List<GeoLoc> getAllGeoLocs() {
		return geoLocHome.findAll();
	}

	public Map<Long, Status> getAllStatus() {
		List<Status> status = statusHome.findAll();
		HashMap<Long, Status> ret = new HashMap<Long, Status>();
		for (Status val : status)
			ret.put(val.getId(), val);
		return ret;
	}

	public Status getStatusFromId(int id) {
		return getAllStatus().get(new Long(id));
	}

	public double quoteForLocation(double lat, double lng) {
		double d = -1d;
		for (GeoLoc loc : getAllGeoLocs()) {
			double dev = distance(loc.getLat(), loc.getLng(), lat, lng);
			if (dev < 5d) {
				d = loc.getBaseVal();
				break;
			}
		}
		return d;
	}

	private double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	public IncidentHome getIncidentHome() {
		return incidentHome;
	}

	public void setIncidentHome(IncidentHome incidentHome) {
		this.incidentHome = incidentHome;
	}

	public StatusHome getStatusHome() {
		return statusHome;
	}

	public void setStatusHome(StatusHome statusHome) {
		this.statusHome = statusHome;
	}

	public GeoLocHome getGeoLocHome() {
		return geoLocHome;
	}

	public void setGeoLocHome(GeoLocHome geoLocHome) {
		this.geoLocHome = geoLocHome;
	}

	public IncSuppHome getIncSuppHome() {
		return incSuppHome;
	}

	public void setIncSuppHome(IncSuppHome incSuppHome) {
		this.incSuppHome = incSuppHome;
	}

	public SupplierHome getSupplierHome() {
		return supplierHome;
	}

	public void setSupplierHome(SupplierHome supplierHome) {
		this.supplierHome = supplierHome;
	}
}
