// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/TilingAdapter.java,v $
// $Revision: 1.5 $ $Date: 2004/10/14 18:06:09 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.List;

/**
 * An interface class for a set of adapter classes that hide the
 * tiling format of a VPF table. None of the methods taking a List
 * parameter may modify the passed in list.
 * 
 * @see DcwRecordFile#getTilingAdapter(String,String)
 */
public interface TilingAdapter {
    /**
     * Get the tile identifier for the alternate tile
     * 
     * @param l a row of the table this adapter was created for
     * @return the tile id of the alternate tile
     */
    public int getTileId(List<Object> l);

    /**
     * Get the primitive identifier in the current tile
     * 
     * @param l a row of the table this adapter was created for
     * @return the primitive id in the current tile
     */
    public int getPrimId(List<Object> l);

    /**
     * Get the primitive identifier in the alternate tile
     * 
     * @param l a row of the table this adapter was created for
     * @return the primitive id in the alternate tile
     */
    public int getTilePrimId(List<Object> l);

    /**
     * Get the full set of primitive information
     * 
     * @param l a row of the table this adapter was created for
     * @return the full cross tile identifier information
     */
    public DcwCrossTileID getPrim(List<Object> l);

    /**
     * A TilingAdapter for untiled data. (The tile_id column doesn't
     * exist, and the primitive column is not a cross-tile
     * identifier.) Alternate tile will always return -1.
     */
    public static class UntiledAdapter implements TilingAdapter {
        /** the primitive column */
        final private int column;

        public UntiledAdapter(int column) {
            this.column = column;
        }

        public int getTileId(List<Object> l) {
            return -1;
        }

        public int getPrimId(List<Object> l) {
            return ((Number) l.get(column)).intValue();
        }

        public int getTilePrimId(List<Object> l) {
            return ((Number) l.get(column)).intValue();
        }

        public DcwCrossTileID getPrim(List<Object> l) {
            return new DcwCrossTileID(getPrimId(l), -1, getTilePrimId(l));
        }
    }

    /**
     * A TilingAdapter for tiled data where the primitive column is a
     * cross-tile identifier.
     */
    public static class CrossTileAdapter implements TilingAdapter {
        /** the primitive column */
        final private int column;

        public CrossTileAdapter(int column) {
            this.column = column;
        }

        public int getTileId(List<Object> l) {
            return getPrim(l).nextTileID;
        }

        public int getPrimId(List<Object> l) {
            return getPrim(l).currentTileKey;
        }

        public int getTilePrimId(List<Object> l) {
            return getPrim(l).nextTileKey;
        }

        public DcwCrossTileID getPrim(List<Object> l) {
            return (DcwCrossTileID) l.get(column);
        }
    }

    /**
     * A TilingAdapter where the tile identifier is retrieved from the
     * tile_id column, and the primitive identifier comes from a
     * numeric column.
     */
    public static class TiledAdapter implements TilingAdapter {
        /** the tile column */
        final private int tilecolumn;
        /** the primitive column */
        final private int primcolumn;

        public TiledAdapter(int tilecolumn, int primcolumn) {
            this.tilecolumn = tilecolumn;
            this.primcolumn = primcolumn;
        }

        public int getTileId(List<Object> l) {
            return ((Number) l.get(tilecolumn)).intValue();
        }

        public int getPrimId(List<Object> l) {
            return getTilePrimId(l);
        }

        public int getTilePrimId(List<Object> l) {
            return ((Number) l.get(primcolumn)).intValue();
        }

        public DcwCrossTileID getPrim(List<Object> l) {
            return new DcwCrossTileID(getPrimId(l), getTileId(l), getTilePrimId(l));
        }
    }
}