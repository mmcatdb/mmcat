package cz.cuni.matfyz.server.utils;

import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachymb.bartik
 */
public class Position implements JSONConvertible {

    private double x;
    private double y;

    public Position() {}
    
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double value) {
        x = value;
    }

    public double getY() {
        return y;
    }

    public void setY(double value) {
        y = value;
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Position> {

        @Override
        protected JSONObject innerToJSON(Position object) throws JSONException {
            var output = new JSONObject();

            output.put("x", object.x);
            output.put("y", object.y);
            
            return output;
        }

    }

    public static class Builder extends FromJSONBuilderBase<Position> {

        @Override
        protected Position innerFromJSON(JSONObject jsonObject) throws JSONException {
            var x = jsonObject.getDouble("x");
            var y = jsonObject.getDouble("y");

            return new Position(x, y);
        }

    }
    
}
