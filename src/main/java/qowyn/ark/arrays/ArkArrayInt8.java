package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import qowyn.ark.ArkArchive;

public class ArkArrayInt8 extends ArrayList<Byte> implements ArkArray<Byte> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayInt8() {}

  public ArkArrayInt8(ArkArchive archive, int dataSize) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getByte());
    }
  }

  public ArkArrayInt8(JsonArray a, int dataSize) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add((byte)n.intValue()));
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size() * Byte.BYTES;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(n -> jab.add(n));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putByte);
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
