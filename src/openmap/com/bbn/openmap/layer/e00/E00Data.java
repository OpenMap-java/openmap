package com.bbn.openmap.layer.e00;

public class E00Data {
    int type,valeur,id,valeur2,ID=-1;

    public E00Data(int id) {
        this.id=id;
    }

    public E00Data( ) {
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" id:");sb.append(id);

        if (ID !=-1) {
            sb.append(" ID:");sb.append(ID);
        }
        
        sb.append(" type:");sb.append(type);
        sb.append(" valeur:");sb.append(valeur);

        if (valeur!=valeur2) {
            sb.append('-');sb.append(valeur2);
        }
        sb.append(' ');
        return sb.toString();
    }
}
