package org.icearchiva.lta.services.aip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icearchiva.lta.service.model.IAIPMetadata;

public class MetsMetadataImpl implements IAIPMetadata {
	
	private Map<String, List<String>> metaItems;
	
	public MetsMetadataImpl() {
		this.metaItems = new HashMap<String, List<String>>();
	}

	@Override
	public Map<String, List<String>> getMetaItems() {
		return metaItems;
	}
	
	public void addMetaItem(String metaitem, List<String> values) {
		metaItems.put(metaitem, values);
	}

}
