type t_color = Red | Blue | Green | Yellow
type t_move = Up | Down | Left | Right
type t_robot = {
    robot_color : t_color;
    mutable pos_x : int;
    mutable pos_y : int;
  }
		 
type t_robots = {
    red : t_robot;
    blue : t_robot;
    green : t_robot;
    yellow : t_robot;
  }
type t_move_sequence = (t_move * t_color) list
type t_case = Empty | Robot of t_color
type t_target = { x : int; y : int; color : t_color; }
type t_grid = t_case array array
type t_game_data = {
    grid : t_grid;
    robots : t_robots;
    target : t_target;
    horizontal_walls : bool array array;
    vertical_walls : bool array array;
  }

(* initializers *)
val make_robot : t_color -> int -> int -> t_robot
val make_robots : t_robot -> t_robot -> t_robot -> t_robot -> t_robots
val make_target : t_color -> int -> int -> t_target
val make_vertical_walls : (int * int) list -> bool array array
val make_horizontal_walls : (int * int) list -> bool array array
val make_grid : t_robots -> t_case array array
				   
(* operators *)			       
val move :
  t_case array array ->
  bool array array -> bool array array -> t_robot ref -> t_move -> unit
val is_target_reached : t_case array array -> t_target -> bool
val move_seq : t_game_data -> (t_move * t_color) list -> bool
val string_of_plateau : bool array array -> bool array array -> string
val string_of_enigme : t_robots -> t_target -> string
val moves_of_string : string -> (t_move * t_color) list
