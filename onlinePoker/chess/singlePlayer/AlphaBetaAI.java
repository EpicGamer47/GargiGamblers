package singlePlayer;


import java.util.Arrays;

import common.AI;
import common.Board;
import common.MultiRanker;
import common.Ranker;

public class AlphaBetaAI extends AI {
	private static final Pair posI = new Pair(Double.POSITIVE_INFINITY);
	private static final Pair negI = new Pair(Double.NEGATIVE_INFINITY);
	
	private static final Ranker r = new MultiRanker(
			new CoverageRanker(1),
			new ValueRanker(.7)
			);
	private int depth;
	
	public AlphaBetaAI(Board b) {
		super(b);
		depth = 10;
	}
	
	public AlphaBetaAI(Board b, int depth) {
		super(b);
		this.depth = depth;
	}
	
	@Override
	public boolean makeAMove() {
		var old = b;
		b = new Board(b);
		var move = alphaBeta(depth, negI, posI, b.turn);
		b = old;
		
		System.out.println(move.val);
		
		if (move.move == null)
			return false;
		
		
		b.move(move.move[0], move.move[1], move.move[2], move.move[3]);
		
		return true;
	}
	
	private static class Pair {
		double val;
		int[] move;
		
		public Pair(double val) {
			this.val = val;
		}
		
		public Pair(double val, int[] move) {
			this.val = val;
			this.move = move;
		}

		public static Pair max(Pair a, Pair b) {
			return a.val > b.val ? a : b;
		}
		
		public static Pair min(Pair a, Pair b) {
			return a.val < b.val ? a : b;
		}
	}
	
	private Pair alphaBeta(int depth, Pair aa, Pair bb, boolean turn) {
		if (b.movesSinceLastCapture + depth / 2 >= 50) {
			return new Pair(0);
		}
		
		if (depth == 0) {
			return new Pair(r.rank(b));
		}
		
		var value = new Pair(turn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
		var moves = b.getAllMoves(turn);
		
//		var error = new int[] {0, 3, -1, 2};
//		for (var m : moves) {
//			if (Arrays.equals(m, error))
//				b.getAllMoves(turn);
//		}
		
		if (moves.size() == 0) {
			if ((b.coverage(!turn) & b.findKing(turn)) == 0) // if stalemate
				return new Pair(0);
			
			return value;
		}
		
		if (turn) {
			for (var m : moves) {
				var d = b.forceMove(m[0], m[1], m[2], m[3], turn);
				
				if (d == null)
					throw new RuntimeException(Arrays.toString(m) + "\n" + 
							(moves.stream()
									.map(a -> Arrays.toString(a) + ", ")
									.reduce("", (curr, next) -> curr + next)) + "\n" + 
							Board.toString(b.white) + "\n" + 
							Board.toString(b.black) + "\n" + 
							b.toString());
				
				var p = alphaBeta(depth - 1, aa, bb, false);
				p.move = m;
				
				value = Pair.max(value, p);
				aa = Pair.max(aa, value);
				b.revert(d);
				
				if (value.val >= bb.val)
					return value;
			}
			
			return value;
		}
		else {
			for (var m : moves) {
				var d = b.forceMove(m[0], m[1], m[2], m[3], turn);
				
				if (d == null)
					throw new RuntimeException(Arrays.toString(m) + "\n" + 
							(moves.stream()
									.map(a -> Arrays.toString(a) + ", ")
									.reduce("", (curr, next) -> curr + next)) + "\n" + 
							Board.toString(b.white) + "\n" + 
							Board.toString(b.black) + "\n" + 
							b.toString());
				
				var p = alphaBeta(depth - 1, aa, bb, true);
				p.move = m;
				
				value = Pair.min(value, p);
				bb = Pair.min(bb, value);
				b.revert(d);
				
				if (value.val <= aa.val)
					return value;
			}
			
			return value;
		}
	}
}
