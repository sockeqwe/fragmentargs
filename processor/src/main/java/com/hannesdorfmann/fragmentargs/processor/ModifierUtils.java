package com.hannesdorfmann.fragmentargs.processor;

import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Utility class to check modifiers
 *
 * @author Hannes Dorfmann
 */
public class ModifierUtils {

  private ModifierUtils() {

  }


  /**
   * Compare the modifier of two elements
   *
   * @return -1 if element a has better visibility, 0 if both have the same visibility, +1 if b has
   * the better visibility. The "best" visibility is PUBLIC
   */
  public static int compareModifierVisibility(Element a, Element b) {


    // a better
    if (a.getModifiers().contains(Modifier.PUBLIC) && !b.getModifiers().contains(Modifier.PUBLIC)) {
      return -1;
    }

    if (isDefaultModifier(a.getModifiers()) && !isDefaultModifier(b.getModifiers())) {
      return -1;
    }

    if (a.getModifiers().contains(Modifier.PROTECTED) && !b.getModifiers().contains(Modifier.PROTECTED)) {
      return -1;
    }


    // b better
    if (b.getModifiers().contains(Modifier.PUBLIC) && !a.getModifiers().contains(Modifier.PUBLIC)) {
      return 1;
    }

    if (isDefaultModifier(b.getModifiers()) && !isDefaultModifier(a.getModifiers())) {
      return 1;
    }

    if (b.getModifiers().contains(Modifier.PROTECTED) && !a.getModifiers().contains(Modifier.PROTECTED)) {
      return 1;
    }

    // Same
    return 0;
  }

  /**
   * @param modifiers Set of modifiers
   * @return true if this element has default visibility
   */
  public static boolean isDefaultModifier(Set<Modifier> modifiers) {
    return !modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED) && !modifiers.contains(Modifier.PRIVATE);
  }


}
