/**
 * @file rexos/mas/data/Position.java
 * @brief Data object for storing a 3d position.
 * @date Created: 22 apr. 2013
 * 
 * @author Peter Bonnema
 * 
 * @section LICENSE
 *          License: newBSD
 * 
 *          Copyright © 2013, HU University of Applied Sciences Utrecht.
 *          All rights reserved.
 * 
 *          Redistribution and use in source and binary forms, with or without
 *          modification, are permitted provided that the following conditions
 *          are met:
 *          - Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *          - Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *          - Neither the name of the HU University of Applied Sciences Utrecht
 *          nor the names of its contributors may be used to endorse or promote
 *          products derived from this software without specific prior written
 *          permission.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *          "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *          LIMITED TO,
 *          THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *          PARTICULAR PURPOSE
 *          ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED
 *          SCIENCES UTRECHT
 *          BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *          OR
 *          CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *          SUBSTITUTE
 *          GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *          INTERRUPTION)
 *          HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *          STRICT
 *          LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *          ANY WAY OUT
 *          OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *          SUCH DAMAGE.
 * 
 **/
package rexos.mas.data;

import java.io.Serializable;

import com.mongodb.BasicDBObject;

/**
 * Data object for storing a 3d position.
 * 
 */
public class Position implements Serializable, MongoSaveable {
	/**
	 * @var long serialVersionUID
	 *      The serialVersionUID for this class.
	 **/
	private static final long serialVersionUID = 7125027379799391886L;

	/**
	 * @var double x
	 *      The x coordinate.
	 **/
	private double x;

	/**
	 * @var double y
	 *      The y coordinate.
	 **/
	private double y;

	/**
	 * @var double z
	 *      The z coordinate
	 **/
	private double z;

	/**
	 * @var int relativeToPart
	 *      The id of the part the coordinates are relative to.
	 **/
	private Part relativeToPart;

	/**
	 * Constructs a default Position object with all fields set to -1.
	 */
	public Position() {
		this(-1, -1, -1, null);
	}

	/**
	 * Constructs a Position object with the specified absolute x, y and z coordinates.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 */
	public Position(double x, double y, double z) {
		this(x, y, z, null);
	}

	/**
	 * Constructs a Position object with the specified absolute x and y coordinates. z is initialized to -1.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public Position(double x, double y) {
		this(x, y, -1, null);
	}

	/**
	 * Constructs a Position object with -1 x, y and z coordinates relative to the specified part.
	 * 
	 * @param relativeToPart The id of the part to which the coordinates are relative.
	 */
	public Position(Part relativeToPart) {
		this(-1, -1, -1, relativeToPart);
	}

	/**
	 * Constructs a Position object with the specified x and y coordinates relative to the specified part. z is
	 * initialized to -1.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param relativeToPart The id of the part to which the coordinates are relative.
	 */
	public Position(double x, double y, Part relativeToPart) {
		this(x, y, -1, relativeToPart);
	}

	/**
	 * Constructs a Position object with the specified x, y and z coordinates relative to the specified part.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param relativeToPart The id of the part to which the coordinates are relative.
	 */
	public Position(double x, double y, double z, Part relativeToPart) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.relativeToPart = relativeToPart;
	}

	/**
	 * Constructs a Position object from the data contained within the specified BasicDBObject.
	 * 
	 * @param object The BasicDBObject containing the required data.
	 */
	public Position(BasicDBObject object) {
		fromBasicDBObject(object);
	}
	
	/**
	 * Adds the x and y value to the x and y value of this position.
	 * 
	 * @param x The x value to add
	 * @param y The y value to add
	 */
	public void translate(double x, double y) {
		translate(x, y, 0);
	}

	/**
	 * Adds the position to this position.
	 * 
	 * @param position The position to add.
	 */
	public void translate(Position position) {
		translate(position.getX(), position.getY(), position.getZ());
	}

	/**
	 * Adds the x, y and z values to the x, y and z values of this position.
	 * 
	 * @param x the x value to add
	 * @param y the y value to add
	 * @param z the z value to add
	 */
	public void translate(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	/**
	 * @see rexos.mas.data.IMongoSaveable#toBasicDBObject()
	 **/
	@Override
	public BasicDBObject toBasicDBObject() {
		BasicDBObject object = new BasicDBObject();
		object.put("x", x);
		object.put("y", y);
		object.put("z", z);
		if(relativeToPart != null) {
			object.put("relativeToPart", relativeToPart.toBasicDBObject());
		}
		return object;
	}

	/**
	 * @see rexos.mas.data.IMongoSaveable#fromBasicDBObject(com.mongodb.BasicDBObject)
	 **/
	@Override
	public void fromBasicDBObject(BasicDBObject object) {
		try {
			BasicDBObject copy = (BasicDBObject) object.copy();
			x = (double) copy.removeField("x");
			y = (double) copy.removeField("y");
			z = (double) copy.removeField("z");
			if(copy.containsField("relativeToPart")) {
				relativeToPart = new Part((BasicDBObject) copy.get("relativeToPart"));
				copy.removeField("relativeToPart");
			}

			if(!copy.isEmpty()) {
				throw new IllegalArgumentException();
			}
		} catch(ClassCastException | NullPointerException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns the x coordinate for this Position.
	 * 
	 * @return The x coordinate.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Sets the x coordinate for this Position.
	 * 
	 * @param x The x coordinate that should be set.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Returns the y coordinate for this Position.
	 * 
	 * @return The y coordinate.
	 */
	public double getY() {
		return y;
	}

	/**
	 * Sets the y coordinate for this Position.
	 * 
	 * @param y The y coordinate that should be set.
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Returns the z coordinate for this Position.
	 * 
	 * @return The z coordinate.
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Sets the z coordinate for this Position.
	 * 
	 * @param z The z coordinate that should be set.
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * Returns the partID of the relative part.
	 * 
	 * @return The partID of the relative part.
	 */
	public Part getRelativeToPart() {
		return relativeToPart;
	}

	/**
	 * Sets the partID of the relative part.
	 * 
	 * @param relativeToPart The partID of the relative part.
	 */
	public void setRelativeToPart(Part relativeToPart) {
		this.relativeToPart = relativeToPart;
	}

	/**
	 * @see java.lang.Object#toString()
	 **/
	@Override
	public String toString() {
		return String.format("Position [x=%s, y=%s, z=%s, relativeToPart=%s]", x, y, z, relativeToPart);
	}
}
