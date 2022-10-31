package UAW.entities.bullet;

import UAW.audiovisual.UAWFx;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.indexer;
import static mindustry.graphics.Drawf.light;

/**
 * Damages all enemies caught within its area of effect
 */
public class AftershockBulletType extends BulletType {
	/**
	 * Delays in tick between splashes
	 */
	public float splashDelay = 25f;
	/**
	 * How many times splash splashDamage occurs
	 */
	public int splashAmount = 3;
	/** How much rotating triangles does the splash effect has */
	public int pointCount = 0;
	public Sound applySound = Sounds.shotgun;
	/** Adjust circle light color1 */
	public Color frontColor = Pal.lightishOrange;
	/** Adjust circle dark color1 */
	public Color backColor = Pal.lightOrange;
	/** Adjust bottom color1 of the circle splash, will use backColor if its null */
	@Nullable
	public Color bottomColor;
	@Nullable
	public Color particleColor;
	public Effect particleEffect = Fx.hitBulletColor;

	float splashDuration = (splashDelay * splashAmount);

	public AftershockBulletType(float splashDamage, float radius) {
		super(splashDamage, radius);
		this.damage = 0f;
		this.splashDamage = splashDamage;
		this.splashDamageRadius = radius;
		this.lifetime = splashDuration;
		scaledSplashDamage = true;
		hitSize = speed = 0;
		smokeEffect = despawnEffect = hitEffect = Fx.none;
		displayAmmoMultiplier = false;
		keepVelocity = false;
		absorbable = false;
		hittable = false;
		collides = false;
	}

	protected static Effect aftershockCircle(float size, float lifetime, Color frontColor, Color backColor, Color splashColor, int pointCount) {
		return new Effect(lifetime, size * 2f, e -> {
			Draw.color(frontColor, backColor, e.fin());
			Lines.stroke(e.fout() * 4f);
			Lines.circle(e.x, e.y, size + e.fout() * 3f - 2f);
			Draw.reset();
			if (pointCount > 0) {
				float offset = Mathf.randomSeed(e.id, 360f);
				for (int i = 0; i < pointCount; i++) {
					float angle = (i * 360f / pointCount + (Time.time * 3)) + (offset + 4);
					float rx = Angles.trnsx(angle, size - 2f), ry = Angles.trnsy(angle, size);
					Draw.color(frontColor, backColor, e.fin());
					Drawf.tri(
						e.x + rx, e.y + ry, 48f, 28f * e.fout(), angle);
				}
			}
			Draw.z(Layer.debris);
			Fill.light(e.x, e.y, Lines.circleVertices(size / 2), size, Color.white.cpy().a(0f), Tmp.c4.set(splashColor).a(e.fout()));
			Draw.reset();
			light(e.x, e.y, size * 1.6f, backColor, e.fout());
		});
	}

	public void generateAftershock(Bullet b) {
		if (b.timer(5, splashDelay) && splashAmount > 1) {
			Damage.damage(b.team, b.x, b.y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround, scaledSplashDamage);
			aftershockCircle(splashDamageRadius, splashDelay, frontColor, backColor, bottomColor == null ? backColor : bottomColor, pointCount).at(b.x, b.y);
			applySound.at(b.x, b.y);
			for (int j = 0; j < ((splashAmount) * 15); j++) {
				particleEffect.at(
					b.x + Angles.trnsx(Mathf.random(360), Mathf.random(splashDamageRadius)),
					b.y + Angles.trnsx(Mathf.random(360), Mathf.random(splashDamageRadius)),
					particleColor == null ? Pal.bulletYellow : particleColor
				);
			}
			if (status != StatusEffects.none) {
				Damage.status(b.team, b.x, b.y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
			}
			if (healPercent > 0f) {
				indexer.eachBlock(b.team, b.x, b.y, splashDamageRadius, Building::damaged, other -> {
					Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
					other.heal(healPercent / 100f * other.maxHealth());
				});
			}
			if (makeFire) {
				indexer.eachBlock(null, b.x, b.y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
			}
		}
	}

	@Override
	public void update(Bullet b) {
		generateAftershock(b);
		super.update(b);
	}
}
