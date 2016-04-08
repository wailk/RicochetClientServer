(* Colors are also used as IDs for the robots*)
type t_color = Red | Blue | Green | Yellow
                                      
type t_move = Up | Down | Left | Right
                                   
type t_robot = {
    robot_color : t_color ;
    mutable pos_x : int;
    mutable pos_y : int
  }
                 
type t_robots = {
    red : t_robot;
    blue : t_robot;
    green : t_robot;
    yellow : t_robot
  }
                  
type t_move_sequence = (t_move * t_color) list

type t_case =
  Empty
  |Robot of t_color
              
type t_target = {x: int; y: int; color: t_color}
                  
type t_grid = t_case array array
                     
type t_game_data = {
    grid : t_grid;
    robots : t_robots;
    target : t_target;
    (* dims 16x15 *)
    horizontal_walls : bool array array;
    (* dims 15x16 *)
    vertical_walls : bool array array;
  }
                     
let make_robot color x y =
  { robot_color=color; pos_x=x; pos_y=y }
    
let make_robots red blue green yellow =
  (*Checking*)
  if red.robot_color <> Red then
    failwith "make_robots: expecting red robot at 1st param"
  else if blue.robot_color <> Blue then
    failwith "make_robots: expecting blue robot at 2nd param"
  else  if green.robot_color <> Green then
    failwith "make_robots: expecting green robot at 3rd param"
  else  if yellow.robot_color <> Yellow then
    failwith "make_robots: expecting yellow robot at 4th param"
  else
    {
      red = red;
      blue = blue;
      green = green;
      yellow = yellow
    }

let make_target color x y = {
    x=x;y=y;color=color
  }
			      
let make_vertical_walls (coords : (int*int) list) =
  let matrix = Array.make_matrix 16 15 false in
  List.iter (fun (x,y) -> matrix.(y).(x) <- true ) coords;
  matrix
    
let make_horizontal_walls (coords : (int*int) list) =
  let matrix = Array.make_matrix 15 16 false in
  List.iter (fun (x,y) -> matrix.(y).(x) <- true) coords;
  matrix

let make_grid robots =
  let grid = Array.make_matrix 16 16 Empty in
  grid.(robots.red.pos_y).(robots.red.pos_x) <- Robot(robots.red.robot_color);
  grid.(robots.blue.pos_y).(robots.blue.pos_x) <- Robot(robots.blue.robot_color);
  grid.(robots.green.pos_y).(robots.green.pos_x) <- Robot(robots.green.robot_color);
  grid.(robots.yellow.pos_y).(robots.yellow.pos_x) <- Robot(robots.yellow.robot_color);
  grid
    
    
    
let rec move_up grid h_walls (x,y) = match (x,y) with
    (i,0) -> (i,0)
   |(i,j) -> if h_walls.(j-1).(i) || grid.(j-1).(i) <> Empty then
               (i,j)
             else
               move_up grid h_walls (i,j-1)
                       
                       
let rec move_down grid h_walls (x,y) = match (x,y) with
    (i,15) -> (i,15)
   |(i,j) -> if h_walls.(j).(i) || grid.(j+1).(i) <> Empty then
               (i,j)
             else
               move_down grid h_walls (i,j+1)

let rec move_right grid v_walls (x,y) = match (x,y) with
    (15,j) -> (15,j)
   |(i,j) -> if v_walls.(j).(i) || grid.(j).(i+1) <> Empty then
               (i,j)
             else
               move_right grid v_walls (i+1,j)

let rec move_left grid v_walls (x,y) = match (x,y) with
    (0,j) -> (0,j)
   |(i,j) -> if v_walls.(j).(i-1) || grid.(j).(i-1) <> Empty then
               (i,j)
             else
               move_right grid v_walls (i+1,j)


                          
let move grid h_walls v_walls robot direction =
  let aux callback walls robot =
    begin
      let new_pos = callback grid walls (robot.contents.pos_x,robot.contents.pos_y) in
      grid.(robot.contents.pos_y).(robot.contents.pos_x) <- Empty;
      grid.(snd new_pos).(fst new_pos) <- Robot (robot.contents.robot_color);
      robot.contents.pos_x <- fst new_pos;
      robot.contents.pos_y <- snd new_pos
    end
  in
  match direction with
    Up -> aux move_up h_walls robot
   |Down -> aux move_down h_walls robot
   |Left -> aux move_left v_walls robot
   |Right -> aux move_right v_walls robot
                 
                 
                 
let is_target_reached grid target = match grid.(target.y).(target.x) with
    Robot (c) -> (c = target.color)
   |_ -> false
           
           
let move_seq game_data moves =
  let grid = game_data.grid
  and robots = game_data.robots
  and h_walls = game_data.horizontal_walls
  and v_walls = game_data.vertical_walls
  in
  let callback (dir,col) = match col with
      Red -> move grid h_walls v_walls (ref robots.red) dir
     |Blue -> move grid h_walls v_walls (ref robots.blue) dir
     |Green -> move grid h_walls v_walls (ref robots.green) dir
     |Yellow -> move grid h_walls v_walls (ref robots.yellow) dir
  in
  List.iter callback moves;
  is_target_reached grid game_data.target



let string_of_color = function
    Red -> "R"
   |Blue -> "B"
   |Green -> "J"
   |Yellow -> "V"

let string_of_plateau h_walls v_walls =
  begin
    let out = ref "" in
    for i = 0 to 15 do
      for j = 0 to 14 do
	if h_walls.(j).(i) then
	  out := !out ^ "("^ (string_of_int i) ^ "," ^ (string_of_int j) ^ ",B)";
      done
    done;
    for i = 0 to 14 do
      for j = 0 to 15 do
	if v_walls.(j).(i) then
	  out := !out ^ "("^ (string_of_int i) ^ "," ^ (string_of_int j) ^ ",G)";
      done
    done;
    !out
  end


let string_of_enigme robots target =
  let coord bot =
    (string_of_int bot.pos_x) ^ "," ^ (string_of_int bot.pos_y)
  in
  "(" ^ (coord robots.red) ^ ","
  ^  (coord robots.blue) ^ ","
  ^  (coord robots.yellow) ^ ","
  ^  (coord robots.green) ^ ","
  ^  (string_of_int target.x) ^ ","
  ^  (string_of_int target.y) ^ ","
  ^ (string_of_color target.color )
  ^ ")"

let moves_of_string move_str =
  let acc = ref [] in
  for i = 0 to (String.length move_str) - 1 do
    if i mod 2 = 1 then
      acc := (String.sub move_str (i-1) 2)::!acc
  done;
  acc := List.rev !acc;
  let aux_col str = match str with
      'R' -> Red
     |'B' -> Blue
     |'j' -> Yellow
     |'V' -> Green
     |_ -> failwith "wrong string for making moveseq" in
  let aux str = match str.[1] with
      'H' -> (Up, aux_col str.[0])
     |'B' -> (Down, aux_col str.[0])
     |'G' -> (Left, aux_col str.[0])
     |'D' -> (Right, aux_col str.[0])
     |_ -> failwith "wrong string for making moveseq"
  in
  List.map aux !acc 
