
public class CameraDescription {
	
	// fields for the camera's attributes
	private float pixel;
	private float minFocalLength;
	private float maxFocalLength;
	private float minAperture;
	private float maxAperture;
	private float minShutterSpeed;
	private float maxShutterSpeed;
	
	public CameraDescription() {
		// initializes all of the fields to 0's
		init();
	}

	// clears all values of the camera by setting them to 0 again
	public void clearAll() {
		init();
	}
	
	// sets the pixel for the camera
	public void setPixel(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			pixel = Float.valueOf((s.substring(0,s.indexOf("^^"))).trim()).floatValue();
		} else {
			pixel = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// sets the min aperture for the camera
	public void setMinAperture(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			minAperture = 
				Float.valueOf((s.substring(0,s.indexOf("^^"))).trim()).floatValue();
		} else {
			minAperture = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// sets the max aperture for the camera
	public void setMaxAperture(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			maxAperture = 
				Float.valueOf((s.substring(0,s.indexOf("^^"))).trim()).floatValue();
		} else {
			maxAperture = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// sets the min shutter speed 
	public void setMinShutterSpeed(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			minShutterSpeed = 
				Float.valueOf((s.substring(0,s.indexOf("^^"))).trim()).floatValue();
		} else {
			minShutterSpeed = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// set the max shutter speed
	public void setMaxShutterSpeed(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			maxShutterSpeed = 
				Float.valueOf((s.substring(0,s.indexOf("^^"))).trim()).floatValue();
		} else {
			maxShutterSpeed = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// sets the focal length
	public void setFocalLength(String s) {
		if ( s == null || s.length() == 0 ) {
			return;
		}
		if ( s.indexOf("^^") > 0 ) {
			s = s.substring(0,s.indexOf("^^"));
		} 
		if ( s.indexOf("mm") > 0 ) {
			s = s.substring(0,s.indexOf("mm"));
		}
		if ( s.indexOf("-") > 0 ) {
			minFocalLength = 
				Float.valueOf((s.substring(0,s.indexOf("-"))).trim()).floatValue();
			maxFocalLength = 
				Float.valueOf((s.substring(s.indexOf("-")+1)).trim()).floatValue();
		} else {
			minFocalLength = maxFocalLength = Float.valueOf(s.trim()).floatValue();
		}
	}
	
	// getter methods for the camera
	public float getPixel() {
		return pixel;
	}
	
	public float getMinFocalLength() {
		return minFocalLength;
	}
	
	public float getMaxFocalLength() {
		return maxFocalLength;
	}
	
	public float getMinAperture() {
		return minAperture;
	}
	
	public float getMaxAperture() {
		return maxAperture;
	}
	
	public float getMinShutterSpeed() {
		return minShutterSpeed;
	}
	
	public float getMaxShutterSpeed() {
		return maxShutterSpeed;
	}
	
	// used to see if both cameras are the same
	public boolean sameAs(CameraDescription cd) {
		
		if ( cd.getPixel() != 0 && pixel != 0 ) { // both are defined
			if ( pixel > cd.getPixel() ) {
				return false;
			}
		}
		
		// returns true if our camera's values are within range of the camera's object we 
		// are comparing it to.
		if ( compareRange(minFocalLength,maxFocalLength,
				cd.getMinFocalLength(),getMaxFocalLength()) == false )  {
			return false;
		}
		
		if ( compareRange(minAperture,maxFocalLength,
				cd.getMinAperture(),cd.getMaxFocalLength()) == false ) {
			return false;
		}
		
		if ( compareRange(minShutterSpeed,maxShutterSpeed,
				cd.getMinShutterSpeed(),cd.getMaxShutterSpeed()) == false ) {
			return false;
		}
		
		return true;
	}
	
	// make sures the min0 and max0 are within range of min1 and max1
	private boolean compareRange(float min0, float max0, float min1, float max1) {
		if ( min0 != 0 && max0 != 0 && min1 != 0 && max1 != 0 ) {
			if ( min0 < min1 || max0 > max1 ) {
				return false;
			}
		} else if ( min0 != 0 && min1 != 0 ) {
			if ( min0 < min1 ) {
				return false;
			}
		} else if ( max0 != 0 && max1 != 0 ) {
			if ( max0 > max1 ) {
				return false;
			}
		}
		return true;
	}
	
	// sets all values of the camera to 0
	private void init() {
		pixel = 0;
		minFocalLength = 0;
		maxFocalLength = 0;
		minAperture = 0;
		maxAperture = 0;
		minShutterSpeed = 0;
		maxShutterSpeed = 0;
	}
	
}
