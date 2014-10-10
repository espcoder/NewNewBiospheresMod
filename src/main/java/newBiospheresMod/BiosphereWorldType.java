/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import net.minecraft.init.Blocks;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;
import newBiospheresMod.Models.ModConfig;

public class BiosphereWorldType extends WorldType
{
	// #region Ownership Tracking

	private static final LruCacheList<World> BiosphereWorlds = new LruCacheList<World>(3, new IKeyProvider<World>()
	{
		@Override
		public Object provideKey(World item)
		{
			return item;
		}
	});

	private static final String IsBiosphereWorldKey = ModConsts.ModId + ".Is Biosphere World";

	public static boolean IsBiosphereWorld(World world)
	{
		if (world != null)
		{
			if (BiosphereWorlds.Contains(world)) { return true; }

			GameRules rules = Utils.GetGameRules(world);
			if (rules != null)
			{
				if (rules.getGameRuleBooleanValue(IsBiosphereWorldKey))
				{
					EnsureWorldIsTracked(world);
					return true;
				}
			}
		}

		return false;
	}

	// #endregion

	public BiosphereWorldType(String s)
	{
		super(s);
	}

	@Override
	public WorldChunkManager getChunkManager(World world)
	{
		// TODO: FIND A WAY TO UNREGISTER THIS IF THE PLAYER LOADS ANOTHER WORLD.
		BiomeGenBase.hell.topBlock = BiomeGenBase.hell.fillerBlock = Blocks.netherrack;
		BiomeGenBase.sky.topBlock = BiomeGenBase.sky.fillerBlock = Blocks.end_stone;

		Blocks.water.setLightOpacity(0);
		Blocks.flowing_water.setLightOpacity(0);

		Blocks.lava.setLightOpacity(0);
		Blocks.flowing_lava.setLightOpacity(0);

		BiosphereWorlds.Push(world);
		return new BiosphereChunkManager(world);
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String params)
	{
		BiosphereWorlds.Push(world);
		return BiosphereChunkProvider.get(world);
	}

	@Override
	public boolean hasVoidParticles(boolean flag)
	{
		return false;
	}

	public int getSeaLevel(World world)
	{
		BiosphereWorlds.Push(world);
		return ModConsts.SEA_LEVEL + 1;
	}

	@Override
	public double voidFadeMagnitude()
	{
		return 1.0D;
	}

	private static void EnsureWorldIsTracked(World world)
	{
		if (world != null)
		{
			BiosphereWorlds.Push(world);
			GameRules rules = Utils.GetGameRules(world);
			if (rules != null)
			{
				rules.addGameRule(IsBiosphereWorldKey, "true");
				ModConfig.get(world).update();
			}
		}
	}
}
