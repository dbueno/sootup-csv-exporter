package sootupexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

enum PredicateFile {
  LANGUAGE("CTADLLanguage"),
  METHOD("Method"),
  CLASS("Class"),
  FORMAL("FormalParam"),
  THIS("ThisVar");

  private final String name;

  PredicateFile(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public Writer getWriter(File directory, String suffix, boolean append) throws IOException {
    File factsFile = new File(directory, name + suffix);
    if (!append) touch(factsFile);
    return new BufferedWriter(new FileWriter(factsFile, true));
  }

  public static void touch(File file) throws IOException {
    file.delete();
    file.createNewFile();
  }
}
