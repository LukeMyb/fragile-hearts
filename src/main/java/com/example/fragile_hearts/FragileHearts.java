package com.example.fragile_hearts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;

public class FragileHearts implements ModInitializer {
    public static final String MOD_ID = "fragile-hearts";

    // 前回の時間を記録する変数（朝が来たかの判定用）
    private long lastTimeOfDay = -1;

    @Override
    public void onInitialize() {
        System.out.println("Fragile Hearts initialized!");



        // 朝の検知とHP減少イベント
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD) return;

            long timeOfDay = world.getTimeOfDay();
            long dayTime = timeOfDay % 24000;

            if (lastTimeOfDay != -1) {
                long lastDayTime = lastTimeOfDay % 24000;

                // 夜から朝になったか、時間スキップ（就寝など）で朝になった瞬間を検知
                boolean isNewMorning = (lastDayTime > 23000 && dayTime < 1000) ||
                        (dayTime < lastDayTime && dayTime < 1000) ||
                        (dayTime - lastDayTime > 1000 && dayTime < 2000);

                if (isNewMorning) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        FragileHeartsPlayer fhPlayer = (FragileHeartsPlayer) player;
                        int hiddenHp = fhPlayer.getHiddenHp();

                        if (hiddenHp > 0) {
                            // 猶予ストックがある場合は消費のみ
                            fhPlayer.removeHiddenHp(1);
                            player.sendMessage(new LiteralText("§e朝が来ました。隠れHPを1消費しました。残り: " + fhPlayer.getHiddenHp()), false);
                        } else {
                            // ストックがない場合、最大HPを1（ハート半分）減少。下限は1
                            EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                            if (maxHealthAttr != null) {
                                double currentMaxHealth = maxHealthAttr.getBaseValue();
                                if (currentMaxHealth > 1.0D) {
                                    maxHealthAttr.setBaseValue(Math.max(1.0D, currentMaxHealth - 1.0D));
                                    // 減った最大HPに合わせて現在HPも調整
                                    if (player.getHealth() > player.getMaxHealth()) {
                                        player.setHealth(player.getMaxHealth());
                                    }
                                    player.sendMessage(new LiteralText("§c朝が来ました。最大HPが減少しました。"), false);
                                }
                            }
                        }
                    }
                }
            }
            lastTimeOfDay = timeOfDay;
        });

        // スポナー破壊イベント
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && state.getBlock() == Blocks.SPAWNER) {
                EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMaxHealth = maxHealthAttr.getBaseValue();
                    if (currentMaxHealth < 6.0D) {
                        // 最大HPが6（ハート3つ）未満なら回復
                        maxHealthAttr.setBaseValue(Math.min(6.0D, currentMaxHealth + 1.0D));
                        player.sendMessage(new LiteralText("§aスポナーを破壊！最大HPが回復しました。"), false);
                    } else {
                        // 最大HPが満タンなら隠れHPストックを増やす
                        FragileHeartsPlayer fhPlayer = (FragileHeartsPlayer) player;
                        if (fhPlayer.getHiddenHp() < 4) {
                            fhPlayer.addHiddenHp(1);
                            player.sendMessage(new LiteralText("§bスポナーを破壊！隠れHPストックを獲得しました。残り: " + fhPlayer.getHiddenHp()), false);
                        } else {
                            player.sendMessage(new LiteralText("§7スポナーを破壊しましたが、隠れHPは既に上限(4)です。"), false);
                        }
                    }
                }
            }
        });
    }
}