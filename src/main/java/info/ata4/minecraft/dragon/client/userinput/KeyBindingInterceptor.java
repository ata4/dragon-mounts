package info.ata4.minecraft.dragon.client.userinput;

/**
* The purpose of this class is to intercept key presses (especially left and right mouse button clicks) and allow
*    greater flexibility in responding to them.
*   The class replaces KeyBindings in GameSettings.  When interception is on:
*      .isPressed() is overridden to return false so that the vanilla code never receives the clicks.
*      .pressed is always false.
*      The true .isPressed() and .pressed are available using .retrieveClick() and .isKeyDown()
*   Usage:
*    (1) replace KeyBinding with a newly generated interceptor
*        eg
*        KeyBindingInterceptor attackButtonInterceptor(GameSettings.keyBindAttack);
*        GameSettings.keyBindAttack = attackButtonInterceptor;
*        This creates an interceptor linked to the existing keyBindAttack.  The original keyBindAttack remains in the
*          KeyBinding hashmap and keyBindArray.
*    (2) Set the interception mode (eg true = on)
*        eg  setInterceptionActive(false);
*    (3) read the underlying clicks using .retrieveClick() or .isUnderlyingKeyDown();
*    (4) when Interceptor is no longer required, call .getOriginalKeyBinding();
*        eg GameSettings.keyBindAttack = attackButtonInterceptor.getOriginalKeyBinding();
*
*  NOTES -
*    (a) the interceptor does not update the .pressed field until .isPressed() is called.  The vanilla Minecraft.runTick
*        currently always accesses .isPressed() before attempting to read .pressed.
*    (b) In the current vanilla code, if the bindings are changed it will affect the original keybinding.  The new binding will
*        be copied to the interceptor at the first call to .retrieveClick(), .isKeyDown(), or .isPressed().
*    (c) Will not work in GUI
 */

import com.google.common.base.Throwables;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KeyBindingInterceptor extends KeyBinding
{
  private static final Field keybindArrayField = ReflectionHelper.findField(KeyBinding.class, "keybindArray", "field_74516_a");
  private static final Field keyCodeField = ReflectionHelper.findField(KeyBinding.class, "keyCode", "field_74512_d");
  private static final Field pressedField = ReflectionHelper.findField(KeyBinding.class, "pressed", "field_74513_e");
  private static final Field pressTimeField = ReflectionHelper.findField(KeyBinding.class, "pressTime", "field_151474_i");

  /**
   *  Create an Interceptor based on an existing binding.
   *  The initial interception mode is OFF.
   *  If existingKeyBinding is already a KeyBindingInterceptor, a reinitialised copy will be created but no further effect.
   * @param existingKeyBinding - the binding that will be intercepted.
   */
  public KeyBindingInterceptor(KeyBinding existingKeyBinding)
  {
    super(existingKeyBinding.getKeyDescription(), existingKeyBinding.getKeyCode(), existingKeyBinding.getKeyCategory());
    try {
      // the base constructor automatically adds the class to the keybindArray and hash, which we don't want, so undo it
      List reflectkeybindArray = (List) keybindArrayField.get(this);
      reflectkeybindArray.remove(this);

      pressedField.setBoolean(this, false);
      pressTimeField.setInt(this, 0);
//      this.pressed = false;
//      this.pressTime = 0;

    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
    this.interceptionActive = false;
    this.interceptedPressTime = 0;

    if (existingKeyBinding instanceof KeyBindingInterceptor) {
      interceptedKeyBinding = ((KeyBindingInterceptor)existingKeyBinding).getOriginalKeyBinding();
    } else {
      interceptedKeyBinding = existingKeyBinding;
    }

    KeyBinding.resetKeyBindingArrayAndHash();
  }

  public void setInterceptionActive(boolean newMode)
  {
    if (newMode && !interceptionActive) {
      this.interceptedPressTime = 0;
    }
    interceptionActive = newMode;
  }

  @Override
  public boolean isKeyDown()
  {
    if (interceptionActive) {
      return false;
    } else {
      return super.isKeyDown();
    }
  }

  public boolean isUnderlyingKeyDown()
  {
    copyKeyCodeToOriginal();
//    return interceptedKeyBinding.pressed;
    try {
      return pressedField.getBoolean(interceptedKeyBinding);
    } catch (Exception e) {
      Throwables.propagate(e);
      return false;
    }
  }

  /**
   *
   * @return returns false if interception isn't active.  Otherwise, retrieves one of the clicks (true) or false if no clicks left
   */
  public boolean retrieveClick()
  {
    copyKeyCodeToOriginal();
    if (interceptionActive) {
      copyClickInfoFromOriginal();

      if (this.interceptedPressTime == 0) {
        return false;
      } else {
        --this.interceptedPressTime;
        return true;
      }
    } else {
      return false;
    }
  }

  /** A better name for this method would be retrieveClick.
   * If interception is on, resets .pressed and .pressTime to zero.
   * Otherwise, copies these from the intercepted KeyBinding.
   * @return If interception is on, this will return false; Otherwise, it will pass on any clicks in the intercepted KeyBinding
   */
  @Override
  public boolean isPressed()
  {
    copyKeyCodeToOriginal();
    copyClickInfoFromOriginal();

    try {

      if (interceptionActive) {
        pressTimeField.setInt(this, 0);
        pressedField.setBoolean(this, false);
//        this.pressTime = 0;
//        this.pressed = false;
        return false;
      } else {
//        if (this.pressTime == 0) {
        if (pressTimeField.getInt(this) == 0) {
          return false;
        } else {
          pressTimeField.setInt(this, pressTimeField.getInt(this) - 1);
//          --this.pressTime;
          return true;
        }
      }
    } catch (Exception e) {
      Throwables.propagate(e);
      return false;
    }
  }

  public KeyBinding getOriginalKeyBinding() {
    return interceptedKeyBinding;
  }

  protected KeyBinding interceptedKeyBinding;
  private boolean interceptionActive;

  private int interceptedPressTime;

  protected void copyClickInfoFromOriginal()
  {
    try {
//      this.pressTime += interceptedKeyBinding.pressTime;
//      this.interceptedPressTime += interceptedKeyBinding.pressTime;
//      interceptedKeyBinding.pressTime = 0;
//      this.pressed = interceptedKeyBinding.pressed;
      int value =  pressTimeField.getInt(this);
      value += pressTimeField.getInt(interceptedKeyBinding);
      pressTimeField.setInt(this, value);
      this.interceptedPressTime += pressTimeField.getInt(interceptedKeyBinding);
      pressTimeField.setInt(interceptedKeyBinding, 0);
      pressedField.setBoolean(this, pressedField.getBoolean(interceptedKeyBinding));
    } catch (Exception e) {
      Throwables.propagate(e);
    }
  }

  protected void copyKeyCodeToOriginal()
  {
    try {
      // only copy if necessary
//      if (this.keyCode != interceptedKeyBinding.keyCode) {
//        this.keyCode = interceptedKeyBinding.keyCode;
      if (keyCodeField.getInt(this) != keyCodeField.getInt(interceptedKeyBinding)) {
        keyCodeField.setInt(this, keyCodeField.getInt(interceptedKeyBinding));
        resetKeyBindingArrayAndHash();
      }
    } catch (Exception e) {
      Throwables.propagate(e);
    }
  }

}
