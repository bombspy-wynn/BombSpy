package xyz.bombspy.client.mixin;

import com.wynntils.models.worlds.BombModel;
import com.wynntils.models.worlds.type.BombInfo;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bombspy.client.mixinterfaces.IBombModelMixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = BombModel.class, remap = false)
public class BombModelMixin implements IBombModelMixin {
    @Override
    public void addBombInfoFromBombSpy(BombInfo bombInfo) {
        try {
            Field bombsField = BombModel.class.getDeclaredField("BOMBS");
            bombsField.setAccessible(true);
            Object bombs = bombsField.get(this);

            Method addMethod = bombs.getClass().getDeclaredMethod("add", BombInfo.class);
            addMethod.setAccessible(true);
            addMethod.invoke(bombs, bombInfo);
        } catch (Exception e) {
            // handle
        }
    }
}
