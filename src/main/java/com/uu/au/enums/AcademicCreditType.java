package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AcademicCreditType {
  @JsonProperty("PROJECT")
  PROJECT(0),
  @JsonProperty("INLUPP1")
  INLUPP1(1),
  @JsonProperty("INLUPP2")
  INLUPP2(2);

  private final int credit;

  @JsonCreator
  public static AcademicCreditType forValues(String credit) {
      switch (credit) {
          case "PROJECT": return AcademicCreditType.PROJECT;
          case "INLUPP1": return AcademicCreditType.INLUPP1;
          case "INLUPP2": return AcademicCreditType.INLUPP2;
          default: return null;
      }
  }

  AcademicCreditType(final int credit) {
    this.credit = credit;
  }
}
