package UAW.world.blocks.gas;

import UAW.content.UAWGas;
import arc.Core;
import gas.GasStack;
import gas.type.Gas;
import mindustry.content.Liquids;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;

import static UAW.Vars.tick;

/** Boils crafter to convert liquid to gas based on conversion ratio */
public class LiquidBoiler extends GasCrafter {

	/** The amount of liquid unit it consumes */
	public float liquidAmount = 30f;
	/** Liquid to gas conversion ratio */
	public float conversionMultiplier = 3f;
	/** Block inventory capacity multipler */
	public float capacityMultiplier = 1.5f;

	public Liquid liquidInput = Liquids.water;
	public Gas gasResult = UAWGas.steam;

	public LiquidBoiler(String name) {
		super(name);
		warmupSpeed = 0.01f;
		craftTime = 1.5f * tick;
		hasItems = true;
		hasLiquids = true;
		hasGasses = true;
		outputsGas = true;
		consumes.liquid(liquidInput, liquidAmount / craftTime);
		outputGas = new GasStack(gasResult, liquidAmount * conversionMultiplier);
	}

	@Override
	public void init() {
		super.init();
		gasCapacity = liquidAmount * conversionMultiplier * capacityMultiplier;
		liquidCapacity = liquidAmount * capacityMultiplier;
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.remove(Stat.productionTime);
	}

	@Override
	public boolean canPlaceOn(Tile tile, Team team, int rotate) {
		if (attribute == null) {
			return super.canPlaceOn(tile, team, rotate);
		} else
			return tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) > 0.01f;
	}

	@Override
	public void setBars() {
		super.setBars();
		bars.add("heat", (LiquidBoilerBuild entity) ->
			new Bar(() ->
				Core.bundle.format("bar.heat", (int) (entity.warmup)),
				() -> Pal.lightOrange,
				entity::warmupProgress
			));
	}

	public class LiquidBoilerBuild extends GasCrafterBuild {

		public float warmupProgress() {
			return warmup;
		}

		@Override
		public float getProgressIncrease(float base) {
			return super.getProgressIncrease(base) * (warmupProgress() * (attribute != null ? efficiencyScale() : 1));
		}

	}
}

