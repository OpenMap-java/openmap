package com.bbn.openmap.layer.rpf;

import java.io.File;

import com.bbn.openmap.layer.util.DataPathWanderer;

/**
 * Adds RPF directories with a A.TOC file inside them to the data paths.
 * @author dfdietrick
  */
public class RpfDataPathWanderer extends DataPathWanderer {

	public RpfDataPathWanderer() {
		setCallback(this);
	}

	public Class<RpfLayer> getDataUserClass() {
		return RpfLayer.class;
	}
	
	@Override
	public void handleDirectory(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleFile(File file) {
		if (file.getName().equalsIgnoreCase("A.TOC")) {
			File parent = file.getParentFile();
			if (parent != null && parent.getName().equalsIgnoreCase("RPF")) {
				addDataPath(parent.getAbsolutePath());
			}
		}
	}

	@Override
	public String getPrettyName() {
		return "RPF Layer";
	}
	
	@Override
	public boolean isMultiPathLayer() {
		return true;
	}
}
