package com.hannesdorfmann.fragmentargs.processor;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Hannes Dorfmann
 */
public class ModifierUtilsDefaultModifierTest {

  @Test
  public void isDefaultModifier() {

    Set<Modifier> modifiers = new HashSet<Modifier>();
    modifiers.add(Modifier.PUBLIC);
    Assert.assertFalse(ModifierUtils.isDefaultModifier(modifiers));

    modifiers.clear();
    modifiers.add(Modifier.PRIVATE);
    Assert.assertFalse(ModifierUtils.isDefaultModifier(modifiers));

    modifiers.clear();
    modifiers.add(Modifier.PROTECTED);
    Assert.assertFalse(ModifierUtils.isDefaultModifier(modifiers));


    modifiers.clear();
    modifiers.add(Modifier.FINAL);
    modifiers.add(Modifier.STATIC);

    Assert.assertTrue(ModifierUtils.isDefaultModifier(modifiers));
  }


}
