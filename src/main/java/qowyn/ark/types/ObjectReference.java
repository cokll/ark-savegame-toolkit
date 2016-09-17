package qowyn.ark.types;

import java.util.Set;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;
import qowyn.ark.GameObjectContainer;
import qowyn.ark.NameContainer;

public class ObjectReference implements NameContainer {

  public static final int TYPE_ID = 0;

  public static final int TYPE_PATH = 1;

  private int length;

  private int objectType;

  private int objectId;

  private ArkName objectString;

  public ObjectReference() {}

  public ObjectReference(ArkArchive archive, int length) {
    this.length = length;
    read(archive);
  }

  public ObjectReference(JsonObject o, int length) {
    this.length = length;
    JsonValue v = o.get("value");
    if (v.getValueType() == ValueType.NUMBER) {
      JsonNumber n = (JsonNumber) v;
      objectId = n.intValue();
      objectType = TYPE_ID;
    } else {
      JsonString s = (JsonString) v;
      objectString = new ArkName(s.getString());
      objectType = TYPE_PATH;
    }
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }

  public ArkName getObjectString() {
    return objectString;
  }

  public void setObjectString(ArkName objectString) {
    this.objectString = objectString;
  }

  public int getObjectType() {
    return objectType;
  }

  public void setObjectType(int objectType) {
    this.objectType = objectType;
  }

  @Override
  public String toString() {
    return "ObjectReference [objectType=" + objectType + ", objectId=" + objectId + ", objectString=" + objectString + ", length=" + length + "]";
  }

  public GameObject getObject(GameObjectContainer objectContainer) {

    if (objectType == TYPE_ID && objectId > -1 && objectId < objectContainer.getObjects().size()) {
      return objectContainer.getObjects().get(objectId);
    }

    return null;
  }

  public JsonObject toJSON() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    if (objectType == TYPE_ID) {
      job.add("value", objectId);
    } else if (objectType == TYPE_PATH) {
      job.add("value", objectString.toString());
    }

    return job.build();
  }

  public int getSize(boolean nameTable) {
    if (objectType == TYPE_ID) {
      return length;
    } else if (objectType == TYPE_PATH) {
      return Integer.BYTES + ArkArchive.getNameLength(objectString, nameTable);
    } else {
      return length;
    }
  }

  public void read(ArkArchive archive) {
    if (length >= 8) {
      objectType = archive.getInt();
      if (objectType == TYPE_ID) {
        objectId = archive.getInt();
      } else if (objectType == TYPE_PATH) {
        objectString = archive.getName();
      } else {
        System.err.println("Warning: ObjectReference with unkown type " + objectType + " at " + Integer.toHexString(archive.position()));
        archive.position(archive.position() + length - 4);
      }
    } else if (length == 4) { // Only seems to happen in Version 5
      objectType = TYPE_ID;
      objectId = archive.getInt();
    } else {
      System.err.println("Warning: ObjectReference with length value " + length + " at " + Integer.toHexString(archive.position()));
      archive.position(archive.position() + length);
    }
  }

  public void write(ArkArchive archive) {
    if (objectType != TYPE_ID || length >= 8) {
      archive.putInt(objectType);
    }

    if (objectType == TYPE_ID) {
      archive.putInt(objectId);
    } else if (objectType == TYPE_PATH) {
      archive.putName(objectString);
    }
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    if (objectType == TYPE_PATH) {
      nameTable.add(objectString.getNameString());
    }
  }

}
