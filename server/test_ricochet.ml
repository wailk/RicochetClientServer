  
(* TEST INSTANCE SAMPLE *)
open Ricochet
  
let main() = 
  let test_vert_walls =
    make_vertical_walls [(0,1);(1,1);(2,1);(3,1);(4,1)] 
  and test_hor_walls =
    make_horizontal_walls [(0,1);(1,1);(2,1);(3,1);(4,1)]
                          
  (* On cree les 4 robtos: *)
  and red_bot = make_robot Red 0 0
  and green_bot = make_robot Green 0 1
  and blue_bot = make_robot Blue 0 2
  and yellow_bot = make_robot Yellow 0 3 in
  
  (* On wrap les robots et on cree la cible *)
  let sample_robots = make_robots red_bot blue_bot green_bot yellow_bot
  and sample_target = make_target Yellow 15 0 in
  
  (* On cree une grille avec les robors *)
  let sample_grid = make_grid sample_robots in
  
  (* On definie un enchainement de coups *)
  let sample_seq = moves_of_string "JBJDJH" in
  
  (* On wrap tout ca dans le type game_data *)
  let sample_game_data = {
      grid = sample_grid;
      robots = sample_robots;
      target = sample_target;
      horizontal_walls = test_hor_walls;
      vertical_walls = test_vert_walls;
    } in
  move_seq sample_game_data sample_seq
