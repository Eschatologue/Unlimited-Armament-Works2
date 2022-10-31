package UAW.entities.effects;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.graphics.Pal;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.randLenVectors;

public class SparkEffect extends Effect {
	public Color sparkColor = Pal.bulletYellow, lerpColor = Color.lightGray;
	public float alpha = 1f;
	public float size = 3, life = 90, spreadBase = 5, spreadRad = 22;
	public int amount = 5;

	public int shape = 2;

	public SparkEffect() {
		clip = spreadRad * 5;
		lifetime = life;

		renderer = e -> {
			alpha(alpha);
			color(sparkColor, lerpColor, e.fin());
			randLenVectors(e.id, amount > 0 ? amount : (int) (size * 1.6f), spreadBase + e.finpow() * spreadRad, (x, y) -> {
				switch (shape) {
					case 1 -> Fill.circle(e.x + x, e.y + y, e.fout() * size);
					case 2 -> Fill.square(e.x + x, e.y + y, e.fout() * size, 45);
					case 3 -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * size);
				}
			});
		};

	}
}
