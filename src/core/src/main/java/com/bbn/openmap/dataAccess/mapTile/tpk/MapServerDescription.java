package com.bbn.openmap.dataAccess.mapTile.tpk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parser object for the MapServer json description in a tpk file. Created using
 * https://timboudreau.com/blog/json/read, based on an example json document
 * downloaded from esri (the ImageryTPK packge).
 * 
 * @author dietrick
 *
 */
public final class MapServerDescription {
	public final String name;
	public final Contents contents;
	public final ResourceInfo resourceInfo;
	public final Resource resources[];

	@JsonCreator
	public MapServerDescription(@JsonProperty("name") String name, @JsonProperty("contents") Contents contents,
			@JsonProperty("resourceInfo") ResourceInfo resourceInfo,
			@JsonProperty(value = "resources", required = false) Resource[] resources) {
		this.name = name;
		this.contents = contents;
		this.resourceInfo = resourceInfo;
		this.resources = resources;
	}

	public String getPathToTiles() {
		String[] versionStringArray = Double.toString(contents.currentVersion).replace('.', ':').split(":");
		StringBuilder v = new StringBuilder();
		for (String vs : versionStringArray) {
			v.append(vs);
		}
		return new StringBuilder("v").append(v).append("/").append(contents.mapName).append("/_alllayers").toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("MapServerDescription[");
		sb.append("mapName:").append(contents.mapName).append(",");
		sb.append("version:").append(contents.currentVersion).append(",");
		sb.append("singleFusedMapCache:").append(contents.singleFusedMapCache);
		sb.append("]");
		return sb.toString();
	}

	public static final class Contents {
		public final double currentVersion;
		public final String serviceDescription;
		public final String mapName;
		public final String description;
		public final String copyrightText;
		public final boolean supportsDynamicLayers;
		public final Layer layers[];
		public final Table tables[];
		public final SpatialReference spatialReference;
		public final boolean singleFusedMapCache;
		public final InitialExtent initialExtent;
		public final FullExtent fullExtent;
		public final long minScale;
		public final long maxScale;
		public final String units;
		public final String supportedImageFormatTypes;
		public final DocumentInfo documentInfo;
		public final String capabilities;
		public final String supportedQueryFormats;
		public final long maxRecordCount;
		public final long maxImageHeight;
		public final long maxImageWidth;

		@JsonCreator
		public Contents(@JsonProperty("currentVersion") double currentVersion,
				@JsonProperty("serviceDescription") String serviceDescription, @JsonProperty("mapName") String mapName,
				@JsonProperty("description") String description, @JsonProperty("copyrightText") String copyrightText,
				@JsonProperty("supportsDynamicLayers") boolean supportsDynamicLayers,
				@JsonProperty("layers") Layer[] layers, @JsonProperty("tables") Table[] tables,
				@JsonProperty("spatialReference") SpatialReference spatialReference,
				@JsonProperty("singleFusedMapCache") boolean singleFusedMapCache,
				@JsonProperty("initialExtent") InitialExtent initialExtent,
				@JsonProperty("fullExtent") FullExtent fullExtent, @JsonProperty("minScale") long minScale,
				@JsonProperty("maxScale") long maxScale, @JsonProperty("units") String units,
				@JsonProperty("supportedImageFormatTypes") String supportedImageFormatTypes,
				@JsonProperty("documentInfo") DocumentInfo documentInfo,
				@JsonProperty("capabilities") String capabilities,
				@JsonProperty("supportedQueryFormats") String supportedQueryFormats,
				@JsonProperty("maxRecordCount") long maxRecordCount,
				@JsonProperty("maxImageHeight") long maxImageHeight,
				@JsonProperty("maxImageWidth") long maxImageWidth) {
			this.currentVersion = currentVersion;
			this.serviceDescription = serviceDescription;
			this.mapName = mapName;
			this.description = description;
			this.copyrightText = copyrightText;
			this.supportsDynamicLayers = supportsDynamicLayers;
			this.layers = layers;
			this.tables = tables;
			this.spatialReference = spatialReference;
			this.singleFusedMapCache = singleFusedMapCache;
			this.initialExtent = initialExtent;
			this.fullExtent = fullExtent;
			this.minScale = minScale;
			this.maxScale = maxScale;
			this.units = units;
			this.supportedImageFormatTypes = supportedImageFormatTypes;
			this.documentInfo = documentInfo;
			this.capabilities = capabilities;
			this.supportedQueryFormats = supportedQueryFormats;
			this.maxRecordCount = maxRecordCount;
			this.maxImageHeight = maxImageHeight;
			this.maxImageWidth = maxImageWidth;
		}

		public static final class Layer {
			public final long id;
			public final String name;
			public final long parentLayerId;
			public final boolean defaultVisibility;
			public final Object subLayerIds;
			public final long minScale;
			public final long maxScale;

			@JsonCreator
			public Layer(@JsonProperty("id") long id, @JsonProperty("name") String name,
					@JsonProperty("parentLayerId") long parentLayerId,
					@JsonProperty("defaultVisibility") boolean defaultVisibility,
					@JsonProperty("subLayerIds") Object subLayerIds, @JsonProperty("minScale") long minScale,
					@JsonProperty("maxScale") long maxScale) {
				this.id = id;
				this.name = name;
				this.parentLayerId = parentLayerId;
				this.defaultVisibility = defaultVisibility;
				this.subLayerIds = subLayerIds;
				this.minScale = minScale;
				this.maxScale = maxScale;
			}
		}

		public static final class Table {

			@JsonCreator
			public Table() {
			}
		}

		public static final class SpatialReference {
			public final long wkid;
			public final long latestWkid;

			@JsonCreator
			public SpatialReference(@JsonProperty("wkid") long wkid, @JsonProperty("latestWkid") long latestWkid) {
				this.wkid = wkid;
				this.latestWkid = latestWkid;
			}
		}

		public static final class InitialExtent {
			public final double xmin;
			public final double ymin;
			public final double xmax;
			public final double ymax;
			public final SpatialReference spatialReference;

			@JsonCreator
			public InitialExtent(@JsonProperty("xmin") double xmin, @JsonProperty("ymin") double ymin,
					@JsonProperty("xmax") double xmax, @JsonProperty("ymax") double ymax,
					@JsonProperty("spatialReference") SpatialReference spatialReference) {
				this.xmin = xmin;
				this.ymin = ymin;
				this.xmax = xmax;
				this.ymax = ymax;
				this.spatialReference = spatialReference;
			}

			public static final class SpatialReference {
				public final long wkid;
				public final long latestWkid;

				@JsonCreator
				public SpatialReference(@JsonProperty("wkid") long wkid, @JsonProperty("latestWkid") long latestWkid) {
					this.wkid = wkid;
					this.latestWkid = latestWkid;
				}
			}
		}

		public static final class FullExtent {
			public final double xmin;
			public final double ymin;
			public final double xmax;
			public final double ymax;
			public final SpatialReference spatialReference;

			@JsonCreator
			public FullExtent(@JsonProperty("xmin") double xmin, @JsonProperty("ymin") double ymin,
					@JsonProperty("xmax") double xmax, @JsonProperty("ymax") double ymax,
					@JsonProperty("spatialReference") SpatialReference spatialReference) {
				this.xmin = xmin;
				this.ymin = ymin;
				this.xmax = xmax;
				this.ymax = ymax;
				this.spatialReference = spatialReference;
			}

			public static final class SpatialReference {
				public final long wkid;
				public final long latestWkid;

				@JsonCreator
				public SpatialReference(@JsonProperty("wkid") long wkid, @JsonProperty("latestWkid") long latestWkid) {
					this.wkid = wkid;
					this.latestWkid = latestWkid;
				}
			}
		}

		public static final class DocumentInfo {
			public final String title;
			public final String author;
			public final String comments;
			public final String subject;
			public final String category;
			public final String antialiasingMode;
			public final String textAntialiasingMode;
			public final String keywords;

			@JsonCreator
			public DocumentInfo(@JsonProperty("Title") String title, @JsonProperty("Author") String author,
					@JsonProperty("Comments") String comments, @JsonProperty("Subject") String subject,
					@JsonProperty("Category") String category,
					@JsonProperty("AntialiasingMode") String antialiasingMode,
					@JsonProperty("TextAntialiasingMode") String textAntialiasingMode,
					@JsonProperty("Keywords") String keywords) {
				this.title = title;
				this.author = author;
				this.comments = comments;
				this.subject = subject;
				this.category = category;
				this.antialiasingMode = antialiasingMode;
				this.textAntialiasingMode = textAntialiasingMode;
				this.keywords = keywords;
			}
		}
	}

	public static final class ResourceInfo {
		public final GeoFullExtent geoFullExtent;
		public final SpatialReferenceDomain spatialReferenceDomain;
		public final GeoInitialExtent geoInitialExtent;

		@JsonCreator
		public ResourceInfo(@JsonProperty("geoFullExtent") GeoFullExtent geoFullExtent,
				@JsonProperty("spatialReferenceDomain") SpatialReferenceDomain spatialReferenceDomain,
				@JsonProperty("geoInitialExtent") GeoInitialExtent geoInitialExtent) {
			this.geoFullExtent = geoFullExtent;
			this.spatialReferenceDomain = spatialReferenceDomain;
			this.geoInitialExtent = geoInitialExtent;
		}

		public static final class GeoFullExtent {
			public final double xmin;
			public final double ymin;
			public final double xmax;
			public final double ymax;

			@JsonCreator
			public GeoFullExtent(@JsonProperty("xmin") double xmin, @JsonProperty("ymin") double ymin,
					@JsonProperty("xmax") double xmax, @JsonProperty("ymax") double ymax) {
				this.xmin = xmin;
				this.ymin = ymin;
				this.xmax = xmax;
				this.ymax = ymax;
			}
		}

		public static final class SpatialReferenceDomain {
			public final long xmin;
			public final long ymin;
			public final long xmax;
			public final long ymax;

			@JsonCreator
			public SpatialReferenceDomain(@JsonProperty("xmin") long xmin, @JsonProperty("ymin") long ymin,
					@JsonProperty("xmax") long xmax, @JsonProperty("ymax") long ymax) {
				this.xmin = xmin;
				this.ymin = ymin;
				this.xmax = xmax;
				this.ymax = ymax;
			}
		}

		public static final class GeoInitialExtent {
			public final double xmin;
			public final double ymin;
			public final double xmax;
			public final double ymax;

			@JsonCreator
			public GeoInitialExtent(@JsonProperty("xmin") double xmin, @JsonProperty("ymin") double ymin,
					@JsonProperty("xmax") double xmax, @JsonProperty("ymax") double ymax) {
				this.xmin = xmin;
				this.ymin = ymin;
				this.xmax = xmax;
				this.ymax = ymax;
			}
		}
	}

	public static final class Resource {
		public final String name;
		public final RResource resources[];
		public final Contents contents;

		@JsonCreator
		public Resource(@JsonProperty("name") String name, @JsonProperty("resources") RResource[] resource,
				@JsonProperty("contents") Contents contents) {
			this.name = name;
			this.resources = resource;
			this.contents = contents;
		}

		public static final class RResource {
			public final String name;
			public final Contents contents;

			@JsonCreator
			public RResource(@JsonProperty("name") String name, @JsonProperty("contents") Contents contents) {
				this.name = name;
				this.contents = contents;
			}

			public static final class Contents {
				public final double currentVersion;
				public final long id;
				public final String name;
				public final String type;
				public final String description;
				public final String definitionExpression;
				public final GeometryType geometryType;
				public final String copyrightText;
				public final ParentLayer parentLayer;
				public final SubLayer subLayers[];
				public final long minScale;
				public final long maxScale;
				public final boolean defaultVisibility;
				public final Extent extent;
				public final boolean hasAttachments;
				public final HtmlPopupType htmlPopupType;
				public final String displayField;
				public final TypeIdField typeIdField;
				public final Fields fields;
				public final Relationship relationships[];
				public final boolean canModifyLayer;
				public final boolean canScaleSymbols;
				public final boolean hasLabels;
				public final String capabilities;
				public final boolean supportsStatistics;
				public final boolean supportsAdvancedQueries;
				public final String supportedQueryFormats;

				@JsonCreator
				public Contents(@JsonProperty("currentVersion") double currentVersion, @JsonProperty("id") long id,
						@JsonProperty("name") String name, @JsonProperty("type") String type,
						@JsonProperty("description") String description,
						@JsonProperty("definitionExpression") String definitionExpression,
						@JsonProperty("geometryType") GeometryType geometryType,
						@JsonProperty("copyrightText") String copyrightText,
						@JsonProperty("parentLayer") ParentLayer parentLayer,
						@JsonProperty("subLayers") SubLayer[] subLayers, @JsonProperty("minScale") long minScale,
						@JsonProperty("maxScale") long maxScale,
						@JsonProperty("defaultVisibility") boolean defaultVisibility,
						@JsonProperty("extent") Extent extent, @JsonProperty("hasAttachments") boolean hasAttachments,
						@JsonProperty("htmlPopupType") HtmlPopupType htmlPopupType,
						@JsonProperty("displayField") String displayField,
						@JsonProperty("typeIdField") TypeIdField typeIdField, @JsonProperty("fields") Fields fields,
						@JsonProperty("relationships") Relationship[] relationships,
						@JsonProperty("canModifyLayer") boolean canModifyLayer,
						@JsonProperty("canScaleSymbols") boolean canScaleSymbols,
						@JsonProperty("hasLabels") boolean hasLabels, @JsonProperty("capabilities") String capabilities,
						@JsonProperty("supportsStatistics") boolean supportsStatistics,
						@JsonProperty("supportsAdvancedQueries") boolean supportsAdvancedQueries,
						@JsonProperty("supportedQueryFormats") String supportedQueryFormats) {
					this.currentVersion = currentVersion;
					this.id = id;
					this.name = name;
					this.type = type;
					this.description = description;
					this.definitionExpression = definitionExpression;
					this.geometryType = geometryType;
					this.copyrightText = copyrightText;
					this.parentLayer = parentLayer;
					this.subLayers = subLayers;
					this.minScale = minScale;
					this.maxScale = maxScale;
					this.defaultVisibility = defaultVisibility;
					this.extent = extent;
					this.hasAttachments = hasAttachments;
					this.htmlPopupType = htmlPopupType;
					this.displayField = displayField;
					this.typeIdField = typeIdField;
					this.fields = fields;
					this.relationships = relationships;
					this.canModifyLayer = canModifyLayer;
					this.canScaleSymbols = canScaleSymbols;
					this.hasLabels = hasLabels;
					this.capabilities = capabilities;
					this.supportsStatistics = supportsStatistics;
					this.supportsAdvancedQueries = supportsAdvancedQueries;
					this.supportedQueryFormats = supportedQueryFormats;
				}

				public static final class GeometryType {

					@JsonCreator
					public GeometryType() {
					}
				}

				public static final class ParentLayer {

					@JsonCreator
					public ParentLayer() {
					}
				}

				public static final class SubLayer {

					@JsonCreator
					public SubLayer() {
					}
				}

				public static final class Extent {
					public final double xmin;
					public final double ymin;
					public final double xmax;
					public final double ymax;
					public final SpatialReference spatialReference;

					@JsonCreator
					public Extent(@JsonProperty("xmin") double xmin, @JsonProperty("ymin") double ymin,
							@JsonProperty("xmax") double xmax, @JsonProperty("ymax") double ymax,
							@JsonProperty("spatialReference") SpatialReference spatialReference) {
						this.xmin = xmin;
						this.ymin = ymin;
						this.xmax = xmax;
						this.ymax = ymax;
						this.spatialReference = spatialReference;
					}

					public static final class SpatialReference {
						public final long wkid;
						public final long latestWkid;

						@JsonCreator
						public SpatialReference(@JsonProperty("wkid") long wkid,
								@JsonProperty("latestWkid") long latestWkid) {
							this.wkid = wkid;
							this.latestWkid = latestWkid;
						}
					}
				}

				public static final class HtmlPopupType {

					@JsonCreator
					public HtmlPopupType() {
					}
				}

				public static final class TypeIdField {

					@JsonCreator
					public TypeIdField() {
					}
				}

				public static final class Fields {

					@JsonCreator
					public Fields() {
					}
				}

				public static final class Relationship {

					@JsonCreator
					public Relationship() {
					}
				}
			}
		}

		public static final class Contents {
			public final Layer layers[];

			@JsonCreator
			public Contents(@JsonProperty("layers") Layer[] layers) {
				this.layers = layers;
			}

			public static final class Layer {
				public final long layerId;
				public final String layerName;
				public final String layerType;
				public final long minScale;
				public final long maxScale;
				public final Legend legend[];

				@JsonCreator
				public Layer(@JsonProperty("layerId") long layerId, @JsonProperty("layerName") String layerName,
						@JsonProperty("layerType") String layerType, @JsonProperty("minScale") long minScale,
						@JsonProperty("maxScale") long maxScale, @JsonProperty("legend") Legend[] legend) {
					this.layerId = layerId;
					this.layerName = layerName;
					this.layerType = layerType;
					this.minScale = minScale;
					this.maxScale = maxScale;
					this.legend = legend;
				}

				public static final class Legend {
					public final String label;
					public final String url;
					public final String imageData;
					public final String contentType;

					@JsonCreator
					public Legend(@JsonProperty("label") String label, @JsonProperty("url") String url,
							@JsonProperty("imageData") String imageData,
							@JsonProperty("contentType") String contentType) {
						this.label = label;
						this.url = url;
						this.imageData = imageData;
						this.contentType = contentType;
					}
				}
			}
		}
	}
}