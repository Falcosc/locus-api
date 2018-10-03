/*  
 * Copyright 2012, Asamm Software, s. r. o.
 * 
 * This file is part of LocusAPI.
 * 
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *  
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see 
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects;

import java.io.IOException;
import java.util.Hashtable;

import locus.api.objects.enums.PointRteAction;
import locus.api.objects.extra.GeoDataExtra;
import locus.api.objects.extra.GeoDataStyle;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;
import locus.api.utils.Utils;

public abstract class GeoData extends Storable {

	// tag for logger
	private static final String TAG = "GeoData";

	/**
	 * Define state of item (read-only/writable)
	 */
	public enum ReadWriteMode {

		/**
		 * Items is in read-only state
		 */
		READ_ONLY,
		/**
		 * Item is in read-write state
		 */
		READ_WRITE
	}

	// PARAMETERS

	// unique ID of this object
	@Deprecated // use get/set instead
	public long id;
	// name of object, have to be unique
	protected String name;
	// time the data was created
	protected long timeCreated;
	
	// extra data with parameters
	public GeoDataExtra extraData;
	
	// style for normal state
	public GeoDataStyle styleNormal;
	// style for highlight state
	public GeoDataStyle styleHighlight;
	// current item state
	private byte mState;

	// define read-write mode of item
	private ReadWriteMode mReadWriteMode;

	/**
	 * Additional temporary storage object. Object is not serialized!
	 * For Locus personal usage only
	 */
	public Object tag;
	private Hashtable<String, Object> mTags;
	
	// temporary variable for sorting
	public int dist;

	// CONSTRUCTORS

	public GeoData() {
		super();
		setBasics();
	}
	
	/**
	 * Set basic parameters.
	 */
	private void setBasics() {
		setEnabled(true);
		setVisible(true);
		setSelected(false);
	}

	// STATE

	/**
	 * Check if item is currently enabled.
	 * @return {@code true} if item is enabled
	 */
	public boolean isEnabled() {
		return isStateValue(0);
	}

	/**
	 * Set item state to "enabled", this means item will be handled in lifecycle of drawing etc.
	 * @param enabled {@code true} to make it enabled
	 */
	public void setEnabled(boolean enabled) {
		setState(0, enabled);
	}

	/**
	 * Check if item is currently visible, so it should be drawn on the map.
	 * @return {@code true} if item is visible
	 */
	public boolean isVisible() {
		return isStateValue(0) && 					// enabled
				isStateValue(1);  					// visible
	}

	/**
	 * Set visibility flag of item.
	 * @param visible {@code true} to make item visible on the map
	 */
	public void setVisible(boolean visible) {
		setState(1, visible);
	}

	/**
	 * Check if item is currently enabled, visible on a map and also selected.
	 * @return {@code true} if item is selected
	 */
	public boolean isSelected() {
		return isStateValue(0) && 					// enabled
				isStateValue(1) && 					// visible
				isStateValue(2);					// selected
	}

	/**
	 * Mark item as selected.
	 * @param selected {@code true} to mark item as selected
	 */
	public void setSelected(boolean selected) {
		setState(2, selected);
	}

	/**
	 * Check state at certain position.
	 * @param position position
	 * @return {@code true} is state is equal 1
	 */
	private boolean isStateValue(int position) {
		return ((mState >> position) & 1) == 1;
	}

	/**
	 * Set certain state in byte.
	 * @param position position of state
	 * @param value value of state
	 */
	private void setState(int position, boolean value) {
		if (value) {
			this.mState |= 1 << position;
		} else {
			this.mState &= ~(1 << position);
		}
	}

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    protected void readExtraData(DataReaderBigEndian dr) throws IOException {
		if (dr.readBoolean()) {
			extraData = new GeoDataExtra();
			extraData.read(dr);
		}
	}
	
	protected void writeExtraData(DataWriterBigEndian dw) throws IOException {
		if (extraData != null && extraData.getCount() > 0) {
			dw.writeBoolean(true);
			dw.writeStorable(extraData);
		} else {
			dw.writeBoolean(false);
		}
	}
	
	protected void readStyles(DataReaderBigEndian dr) throws IOException {
		if (dr.readBoolean()) {
			styleNormal = new GeoDataStyle();
			styleNormal.read(dr);
		}
		if (dr.readBoolean()) {
			styleHighlight = new GeoDataStyle();
			styleHighlight.read(dr);
		}
	}
	
	protected void writeStyles(DataWriterBigEndian dw) throws IOException {
		if (styleNormal != null) {
			dw.writeBoolean(true);
			dw.writeStorable(styleNormal);
		} else {
			dw.writeBoolean(false);
		}
		
		if (styleHighlight != null) {
			dw.writeBoolean(true);
			dw.writeStorable(styleHighlight);
		} else {
			dw.writeBoolean(false);
		}
	}
	
	/**************************************************/
	// GET & SET METHODS
	/**************************************************/

	// ID

	/**
	 * Get ID of current item.
	 * @return ID of item
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set new ID to current item.
	 * @param id value to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	// NAME

	/**
	 * Get name of item.
	 * @return name of item
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set new name for current item.
	 * @param name new name
	 */
	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		this.name = name;
	}

	// TIME CREATED

	/**
	 * Get time when item was created.
	 * @return time of creation [in ms]
	 */
	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * Set new time when item was created.
	 * @param time time of creation [in ms]
	 */
	public void setTimeCreated(long time) {
		this.timeCreated = time;
	}

	// TAGS

	/**
	 * Get tag attached to object, defined by "key".
	 * @param key key value that defined object
	 * @return required object otherwise 'null'
	 */
	public Object getTag(String key) {
		// check key
		if (key == null || key.length() == 0) {
			Logger.logW(TAG, "getTag(" + key + "), " +
					"invalid key");
			return null;
		}

		// get tag
		if (mTags == null) {
			return null;
		} else {
			return mTags.get(key);
		}
	}

	/**
	 * Set new tag/object defined by key.
	 * @param key key that define object
	 * @param value object itself or 'null' if we wants to remove it
	 */
	public void setTag(String key, Object value) {
		// check key
		if (key == null || key.length() == 0) {
			Logger.logW(TAG, "setTag(" + key + "), " +
					"invalid key");
			return;
		}

		// set tag
		if (value == null) {
			if (mTags != null) {
				mTags.remove(key);
			}
		} else {
			if (mTags == null) {
				mTags = new Hashtable<>();
			}
			mTags.put(key, value);
		}
	}

	// EXTRA DATA

	/**
	 * Get extra data serialized into byte array.
	 * @return serialized extra data or 'null' if data doesn't exists
	 */
	public byte[] getExtraDataRaw() {
		try {
			DataWriterBigEndian dw = new DataWriterBigEndian();
			writeExtraData(dw);
			return dw.toByteArray();
		} catch (IOException e) {
			Logger.logE(TAG, "getExtraDataRaw()", e);
			return null;
		}
	}

	/**
	 * Set (load) extra data from byte array.
	 * @param data data
	 */
	public void setExtraDataRaw(byte[] data) {
		try {
			readExtraData(new DataReaderBigEndian(data));
		} catch (Exception e) {
			Logger.logE(TAG, "setExtraDataRaw(" + data + ")", e);
			extraData = null;
		}
	}
	
	// EXTRA DATA - PARAMETERS

	/**
	 * Check if container has any extra data parameters.
	 * @return {@code true} if extraData object is available
	 */
	public boolean hasExtraData() {
		return extraData != null;
	}

	// these are helper functions for more quick access
	// to parameter values without need to check state
	// of GeoDataExtra object
	
	public boolean addParameter(int paramId, String param) {
		// check extra data
		boolean created = createExtraData();

		// add parameter and return result
		return afterItemAdded(extraData.addParameter(paramId, param), created);
    }

    public boolean addParameter(int paramId, byte[] param) {
		// check extra data
		boolean created = createExtraData();

		// add parameter and return result
		return afterItemAdded(extraData.addParameter(paramId, param), created);
    }

    public boolean addParameter(int paramId, int value) {
		// check extra data
		boolean created = createExtraData();

		// add parameter and return result
		return afterItemAdded(extraData.addParameter(paramId, Integer.toString(value)), created);
    }
	
	public boolean addParameter(int paramId, byte param) {
		// check extra data
		boolean created = createExtraData();

		// add parameter and return result
		return afterItemAdded(extraData.addParameter(paramId, param), created);
	}

    /**
     * Return parameter stored in extraData container.
     * @param paramId ID of parameter
     * @return loaded value (length() bigger then 0) or 'null' in case, parameter do not exists
     */
	public String getParameter(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.getParameter(paramId);
	}
	
	public byte[] getParameterRaw(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.getParameterRaw(paramId);
	}

    /**
     * Check if current object has parameter defined by it's ID.
     * @param paramId ID of parameter
     * @return <code>true</code> if non-empty parameter exists
     */
	public boolean hasParameter(int paramId) {
	    // check existence of container
		if (extraData == null) {
			return false;
		}

        // check container itself
		return extraData.hasParameter(paramId);		
	}
	
	public String removeParameter(int paramId) {
		if (extraData == null) {
			return null;
		}
		return extraData.removeParameter(paramId);
	}

    /**************************************************/
	// SHORTCUTS FOR MOST USEFUL PARAMS
    /**************************************************/

    // PARAMETER 'SOURCE'

    /**
     * Get current defined source parameter.
     * @return source parameter or 'SOURCE_UNKNOWN' if not defined
     */
    public byte getParameterSource() {
        if (extraData == null) {
            return GeoDataExtra.SOURCE_UNKNOWN;
        }
        byte[] res = extraData.getParameterRaw(GeoDataExtra.PAR_SOURCE);
        if (res == null || res.length != 1) {
            return GeoDataExtra.SOURCE_UNKNOWN;
        } else {
            return res[0];
        }
    }

    /**
     * Set parameter that define source of waypoint.
     * @param source source ID
     */
	public void setParameterSource(byte source) {
		addParameter(GeoDataExtra.PAR_SOURCE, source);
	}

    /**
     * Check if waypoint source parameter is equal to expected value.
     * @param expectedSource source we are checking
     * @return <code>true</code> if waypoint source if same as expected
     */
	public boolean isParameterSource(byte expectedSource) {
		return getParameterSource() == expectedSource;
	}

    /**
     * Remove existing source parameter.
     */
	public void removeParameterSource() {
		removeParameter(GeoDataExtra.PAR_SOURCE);
	}
	
	// PARAMETER 'STYLE'

	public String getParameterStyleName() {
		if (extraData == null) {
			return "";
		}
		return extraData.getParameter(GeoDataExtra.PAR_STYLE_NAME);
	}

	public void setParameterStyleName(String style) {
		addParameter(GeoDataExtra.PAR_STYLE_NAME, style);
	}
	
	public void removeParameterStyleName() {
		if (extraData == null) {
			return;
		}
		extraData.removeParameter(GeoDataExtra.PAR_STYLE_NAME);
	}

    // PARAMETER 'DESCRIPTION'

    /**
     * Check if item has description parameter.
     * @return <code>true</code> if any description exists
     */
    public boolean hasParameterDescription() {
        return getParameterDescription().length() > 0;
    }

    /**
     * Get description parameter attached to this object.
     * @return description parameter of empty String if not defined
     */
    public String getParameterDescription() {
        if (extraData == null) {
            return "";
        }
        return extraData.getParameterNotNull(GeoDataExtra.PAR_DESCRIPTION);
    }

    /**
     * Set new description value to this object.
     * @param desc new description value
     */
    public void setParameterDescription(String desc) {
        addParameter(GeoDataExtra.PAR_DESCRIPTION, desc);
    }

	// PARAMETER 'RTE ACTION'

	/**
	 * Get action defined for current point.
	 * @return routing action
	 */
	public PointRteAction getParameterRteAction() {
		String param = getParameter(GeoDataExtra.PAR_RTE_POINT_ACTION);
		if (param != null && param.length() > 0) {
			return PointRteAction.getActionById(Utils.parseInt(param));
		} else {
			return PointRteAction.UNDEFINED;
		}
	}

	/**
	 * Set Rte action for current point.
	 * @param action action
	 */
	public void setParameterRteAction(PointRteAction action) {
		// check action
		if (action == null) {
			Logger.logW(TAG, "setParameterRteAction(), " +
					"attempt to set invalid parameter");
			return;
		}

		// store value
		addParameter(GeoDataExtra.PAR_RTE_POINT_ACTION, action.getId());
	}

    // PARAMETER 'EMAIL'

	/**
	 * Add "email" to current object.
	 * @param email email to add
	 */
	public void addEmail(String email) {
		addEmail(null, email);
	}

	/**
	 * Add "email" to current object.
	 * @param label label visible in app
	 * @param email email to add
	 */
	public void addEmail(String label, String email) {
		// check extra data
		boolean created = createExtraData();

		// add parameter
		afterItemAdded(extraData.addEmail(label, email), created);
	}

    // PARAMETER 'PHONE'

	public void addPhone(String phone) {
		addPhone(null, phone);
	}
	
	public void addPhone(String label, String phone) {
		// check extra data
		boolean created = createExtraData();

		// add parameter
		afterItemAdded(extraData.addPhone(label, phone), created);
	}

    // PARAMETER 'URL'

	public void addUrl(String url) {
		addUrl(null, url);
	}
	
	public void addUrl(String label, String url) {
		// check extra data
		boolean created = createExtraData();

		// add parameter
		afterItemAdded(extraData.addUrl(label, url), created);
	}

    // PARAMETER 'AUDIO' ATTACHMENT

    /**
     * Add audio to current object.
     * @param uri uri to add
     * @return <code>true</code> if audio was correctly added
     */
    public boolean addAttachmentAudio(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addAudio(uri), created);
    }

    // PARAMETER 'PHOTO' ATTACHMENT

    /**
     * Add photo to current object.
     * @param uri uri to add
     * @return <code>true</code> if photo was correctly added
     */
	public boolean addAttachmentPhoto(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addPhoto(uri), created);
	}

    // PARAMETER 'VIDEO' ATTACHMENT

    /**
     * Add video to current object.
     * @param uri uri to add
     * @return <code>true</code> if video was correctly added
     */
    public boolean addAttachmentVideo(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addVideo(uri), created);
    }

    // PARAMETER 'OTHER' ATTACHMENT

    /**
     * Add video to current object.
     * @param uri uri to add
     * @return <code>true</code> if video was correctly added
     */
    public boolean addAttachmentOther(String uri) {
        // check extra data
        boolean created = createExtraData();

        // add parameter and return result
        return afterItemAdded(extraData.addOtherFile(uri), created);
    }

	/**
	 * Get parameter that define index of point in track (index of trackpoint).
	 * @return index in track or '-1' if no index is defined
	 */
	public int getParamRteIndex() {
		// get parameter from container
		String parIndex = getParameter(GeoDataExtra.PAR_RTE_INDEX);
		if (parIndex != null) {
			return Utils.parseInt(parIndex);
		}
		return -1;
	}
	
	// STYLES
	
	public byte[] getStyles() {
		try {
			DataWriterBigEndian dw = new DataWriterBigEndian();
			writeStyles(dw);
			return dw.toByteArray();
		} catch (IOException e) {
			Logger.logE(TAG, "getStylesRaw()", e);
			return null;
		}
	}

	public void setStyles(byte[] data) {
		try {
			readStyles(new DataReaderBigEndian(data));
		} catch (Exception e) {
			Logger.logE(TAG, "setExtraStyle(" + data + ")", e);
			extraData = null;
		}
	}

	// READ-WRITE MODE

	/**
	 * Get current defined read-write mode.
	 * @return current mode
	 */
	public ReadWriteMode getReadWriteMode() {
		if (mReadWriteMode == null) {
			return ReadWriteMode.READ_WRITE;
		}
		return mReadWriteMode;
	}

	/**
	 * Set mode for current item.
	 * @param mode new mode
	 */
	public void setReadWriteMode(ReadWriteMode mode) {
		this.mReadWriteMode = mode;
	}

    // EXTRA DATA TOOLS

    /**
     * Check if extra data exists and if not, create it.
     * @return <code>true</code> if container was created
     */
    private boolean createExtraData() {
        if (extraData == null) {
            extraData = new GeoDataExtra();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Do some post-process work after item is added to container.
     * @param added <code>true</code> if item was added
     * @param created <code>true</code> if extra data were created before
     * @return <code>true</code> if all went well and item is stored
     */
    private boolean afterItemAdded(boolean added, boolean created) {
        // add parameter and return result
        if (added) {
            return true;
        } else {
            if (created) {
                extraData = null;
            }
            return false;
        }
    }
}
