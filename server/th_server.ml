
(** ocamlc -thread unix.cma threads.cma str.cma ricochet.cmo th_server.ml -o serv.exe **)


open Ricochet

(* loop breakers *)
exception Client_disconnect
exception Break
exception Winner of int
		      
type t_phase = Waiting_clients | Reflexion | Enchere | Resolution

type t_user_state = Not_connected | Playing | Waiting


(* GLOBAL DATA *)
type t_data = {
    mutable clients : Thread.t list;
    (* list of username,inchan,outchan *)
    users : (string * in_channel * out_channel) array;
    (* connected users *)
    active_users : bool array;
    (* user states *)
    user_states : t_user_state array;
    (* maximum number of users*)
    max_users : int;
    (* index: client_id , content: coups *)
    encheres : int array;
    
    scores : int array ;
    
    mutable phase : t_phase;

    (* game data *)
    mutable game : t_game_data;

    (* reflexion duration *)
    reflexion_duration : float;

    auction_duration : float;

    mut_enchere : Mutex.t;

    mut_data : Mutex.t;

    mutable session_started : bool;

    mutable tour : int;

    mutable won : bool;

    mutable trouve : bool
  }

(* Creates a server and returns its socket *)
let make_server port max_con =
  let sock = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0
  and addr = Unix.inet_addr_of_string "0.0.0.0" in
  Unix.setsockopt sock Unix.SO_REUSEADDR true;
  Unix.bind sock (Unix.ADDR_INET(addr, port));  
  Unix.listen sock max_con;
  sock;;




(* GAME DATA *)
let game_data_example () = 
  let test_vert_walls =
    make_vertical_walls [(0,1);(1,1);(2,1);(3,1);(4,1)] 
  and test_hor_walls =
    make_horizontal_walls [(0,1);(1,1);(2,1);(3,1);(4,1);]
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
  let _ = [(Down,Yellow);(Right,Yellow);(Up,Yellow)] in
  
  (* On wrap tout ca dans le type game_data *)
  let sample_game_data = {
      grid = sample_grid;
      robots = sample_robots;
      target = sample_target;
      horizontal_walls = test_hor_walls;
      vertical_walls = test_vert_walls;
    } in
  sample_game_data


    
(* UTILS *)
let report_error channel message =
  output_string channel ("[ERROR] " ^ message  ^ "\n");
  flush channel


let nb_active data =
  let count = ref 0 in
  for i=0 to Array.length data.contents.active_users - 1 do
    if data.contents.active_users.(i) then
      count := !count + 1
  done;
  !count

(* recherche une case libre et rend son indice *)
let new_user_id data =
  let count = ref 0 in
  try
    for i=0 to Array.length data.contents.active_users - 1 do
      if not data.contents.active_users.(i) then
	raise Break
      else
	count := !count + 1;
    done;
    failwith "this should never happen!"
  with _ -> !count

let notify_active_clients data command =
  for i=0 to Array.length data.contents.users - 1 do
    if data.contents.active_users.(i)  then
      match data.contents.users.(i) with
        (userN,ic,oc) ->
	output_string oc command;
        flush oc;
  done
    

    
(** AUXIALIARY FUNCTIONS **)

let aux_connection inchan outchan client_id data username  = 
  begin
    if data.contents.active_users.(client_id) then
      report_error outchan "already_connected"
    else
      begin
        data.contents.users.(client_id) <- (username,inchan,outchan);
        data.contents.active_users.(client_id) <- true;
        output_string outchan ("BIENVENUE/"^username^"/\n");
        flush outchan; 
        for i=0 to Array.length data.contents.users - 1 do
          if data.contents.active_users.(i) && i <> client_id then
            begin
              match data.contents.users.(i) with
                (userN,ic,oc) -> output_string oc ("CONNECTE/"^username^"/\n");
                                 flush oc;
            end
        done;

	if data.contents.phase = Waiting_clients then
	  begin
	    data.contents.user_states.(client_id) <- Playing;
	    if (nb_active data) > 1 then
	      data.contents.phase <- Reflexion
	  end
	else if data.contents.phase <> Waiting_clients then
	  data.contents.user_states.(client_id) <- Waiting
      end
  end

    
let aux_sort inchan outchan client_id data username =
  begin
    data.contents.active_users.(client_id) <- false;
    (* Notify other active clients*)
    for i=0 to Array.length data.contents.users - 1 do
      if data.contents.active_users.(i) && i <> client_id then
        match data.contents.users.(i) with
          (userN,ic,oc) -> output_string oc ("DECONNEXION/"^username^"/\n");
                           flush oc;
    done;
    if (nb_active data) < 2 then
      data.contents.phase <- Waiting_clients ;
    raise Client_disconnect
  end

    
let aux_enchere inchan outchan client_id data username coups =
  (* CHECK PHASE *)
  if data.contents.phase <> Enchere || data.contents.user_states.(client_id) <> Playing then
    begin
      output_string outchan "ERROR/NOT_ALLOWED/\n" ;
      flush outchan;
    end
      (* Check if valid *)
  else if data.contents.encheres.(client_id) > coups then
    begin
      (* Register bid and confirm *)
      data.contents.encheres.(client_id) <- coups;
      output_string outchan "VALIDATION/\n";
      flush outchan;
      
      (* Notify other clients *)
      for i=0 to Array.length data.contents.users - 1 do
	if data.contents.active_users.(i) && i <> client_id then
          match data.contents.users.(i) with
            (userN,ic,oc) ->
	    output_string oc ("NOUVELLEENCHERE/"^username^"/"^(string_of_int coups)^"/\n");
            flush oc;
      done
    end
  else
    begin  
      output_string outchan ("ECHEC/"^ username  ^ "/\n");
      flush outchan
    end
      
let aux_trouve inchan outchan client_id data username coups =
  (* CHECK PHASE *)
  if data.contents.phase <> Reflexion || data.contents.user_states.(client_id) <> Playing then
    begin
      output_string outchan "ERROR/NOT_ALLOWED/";
      flush outchan;
    end
      (* Check if valid *)
  else if coups > 0 then
    begin
      (* Register bid and confirm *)
      data.contents.encheres.(client_id) <- coups;
      
      output_string outchan "TUASTROUVE/\n";
      flush outchan;
      
      (* Notify other clients *)
      for i=0 to Array.length data.contents.users - 1 do
	if data.contents.active_users.(i) && i <> client_id then
          match data.contents.users.(i) with
            (userN,ic,oc) ->
	    output_string oc ("ILATROUVE/"^username^"/"^(string_of_int coups)^"/\n");
            flush oc;
      done;
      data.contents.trouve <- true
    end
  else
    begin  
      output_string outchan ("ECHEC/"^ username  ^ "/\n");
      flush outchan
    end

let aux_solution inchan outchan client_id data username solution =
  (* CHECK PHASE *)
  if data.contents.phase <> Resolution || data.contents.user_states.(client_id) <> Playing then
    begin
      output_string outchan "ERROR/NOT_ALLOWED/";
      flush outchan;
    end
      (* Check if valid *)
  else
    begin
      (* Notify other clients *)
      for i=0 to Array.length data.contents.users - 1 do
	if data.contents.active_users.(i) && i <> client_id then
          match data.contents.users.(i) with
            (userN,ic,oc) ->
	    output_string oc ("SASOLUTION/"^ username ^ "/" ^ solution  ^ "/\n" );
	    flush oc;
      done;

      (* Check if valid *)
      let sequence = moves_of_string solution in
      if data.contents.encheres.(client_id) <= List.length sequence
	 && (move_seq data.contents.game sequence) then
	data.contents.won <- true
    end



      
let aux_chat_ext data user message =
  begin
    notify_active_clients data ("LISTEN/"^user^ "/"^message^"\n")
  end


    
(* PLAYER SERVICE *)                                                              
let player_service inchan outchan client_id data =
  while true do
    (* RECEIVE *)
    let line = (String.trim (input_line inchan)) in
    match line with
      cmd when Str.string_match (Str.regexp "^\\([A-Z]+\\)/\\([^/]+\\)/\\([^/]*\\)") cmd 0 ->
      (
	let group_1 = (Str.matched_group 1 cmd)
	and group_2 = (Str.matched_group 2 cmd)
	and group_3 = (Str.matched_group 3 cmd) in
	begin
	  (match group_1 with
             "CONNEXION" ->
	     Mutex.lock data.contents.mut_data;
	     aux_connection inchan outchan client_id data group_2;
	     Mutex.unlock data.contents.mut_data;
	    |"SORT"->
	      Mutex.lock data.contents.mut_data;
	      aux_sort inchan outchan client_id data group_2;
	      Mutex.unlock data.contents.mut_data
	    |"ENCHERE" ->
	      Mutex.lock data.contents.mut_data;
	      aux_enchere inchan outchan client_id data group_2 (int_of_string group_3);
	      Mutex.unlock data.contents.mut_data;
	    |"TROUVE" ->
	      Mutex.lock data.contents.mut_data;
	      aux_trouve inchan outchan client_id data group_2 (int_of_string group_3);
	      Mutex.unlock data.contents.mut_data;
	    |"SOLUTION" ->
	      Mutex.lock data.contents.mut_data;
	      aux_solution inchan outchan client_id data group_2 group_3;
	      Mutex.unlock data.contents.mut_data
	    |"CHAT" ->
	      aux_chat_ext data group_2 group_3
	    |_ -> ());
	end
      )
     |invalid_cmd ->
       begin
         output_string outchan ("ERROR/unknowncmd/" ^ invalid_cmd ^ "/\n") ;
         flush outchan;
       end
  done


(* thread function for each user *)
let connection_instance_thread (service, inchan, outchan, sock, user_id, data) =
  while true do
    try
      service inchan outchan user_id data
    with
      Client_disconnect ->
      begin
	data.contents.active_users.(user_id) <- false;
        close_out outchan;
        close_in inchan;
        Unix.close sock;
        Thread.exit ()
      end
  done

let username_by_id data id =
  match data.contents.users.(id) with
    (userN,_,_) -> userN
		     
let bilan data tour =
  let out = ref "" in
  for i=0 to Array.length data.contents.users - 1 do
    if data.contents.active_users.(i) && data.contents.user_states.(i) = Playing then
      match data.contents.users.(i) with
        (userN,_,_) -> out := ("(" ^ userN ^ "," ^ (string_of_int data.contents.scores.(i))
			       ^ ")" ^ !out) 
  done;
  ((string_of_int tour) ^ !out)




let get_lowest_bidder data =
  let index = ref 0
  and min_val = ref max_int in
  for i=0 to Array.length data.contents.users - 1 do
    if data.contents.active_users.(i) && data.contents.user_states.(i) = Playing then
      if  data.contents.encheres.(i) < !min_val then
	begin
	  index := i;
	  min_val := data.contents.encheres.(i)
	end
  done;
  !index


(* returns a list of ( username * user_id * bid ) *)
let ordered_bid_list data =
  let bid_list = Array.to_list data.contents.encheres in
  let bid_user_assoc = List.mapi (fun i bid -> (i,bid)) bid_list in
  (* we stick an id which is the position then we sort the list *)
  List.sort (fun e1 e2 -> (fst e1) - (fst e2)) bid_user_assoc
	    

	    
let game_manager_thread data =
  while true do

    match data.contents.phase with

      

      
      Waiting_clients -> 
      (* On verifie toutes les 10 secondes
       NB: La phase sera changer par le 2e client connecté *)
      Thread.delay 10.

		   
     |Reflexion ->
       
       begin
	 Mutex.lock data.contents.mut_data;
	 print_endline "Phase de reflexion ...\n";
	 (* DEBUT d'une session *)
	 if not data.contents.session_started then
	   (let session =  ("SESSION/"^(string_of_plateau data.contents.game.horizontal_walls
							  data.contents.game.vertical_walls)
			    ^ "/\n") in
	    notify_active_clients data session;
	    data.contents.session_started <- true);
	 let robots = data.contents.game.robots
	 and target = data.contents.game.target in
	 let tour = ("TOUR/" ^ (string_of_enigme robots target) ^ "/" ^
		       (bilan data (data.contents.tour) ) ^ "\n" ) in
	 notify_active_clients data tour;
	 Mutex.unlock data.contents.mut_data;
	 (* dors pendant phase *)
	 begin
	   try
	     for i = 0 to int_of_float data.contents.reflexion_duration do
	       Mutex.lock data.contents.mut_data;
	       let trouve = data.contents.trouve in
	       Mutex.unlock data.contents.mut_data;
	       if not trouve then
		 Thread.delay 1.0
	       else
		 raise Break;
	     done
	   with Break -> ();
	 end;
	 (* notifier fin de reflexion *)
	 notify_active_clients data "FINREFLEXION/\n";
	 Mutex.lock data.contents.mut_data;
	 (* changement de phase *)
	 data.contents.phase <- Enchere;
	 Mutex.unlock data.contents.mut_data;
       end
	 
     |Enchere ->
       begin
	 
	 print_endline "Phase d'encheres ...\n";
	 (* dors pendant phase *)
	 
	 Thread.delay data.contents.auction_duration;
	 Mutex.lock data.contents.mut_data;
	 (* notifier fin de phase d'enchere *)
	 let best_bidder_id = get_lowest_bidder data in
	 let fin_enchere = match data.contents.users.(best_bidder_id) with
	     (userN,_,_) -> "FINENCHERE/" ^ userN ^ "/" ^
			      (string_of_int (data.contents.encheres.(best_bidder_id)))
			      ^ "/\n"
	 in
	 notify_active_clients data fin_enchere; 
	 (* changement de phase *)
	 data.contents.phase <- Resolution;
	 Mutex.unlock data.contents.mut_data
       end

	 
     |Resolution ->
       begin
	 print_endline "Phase de Resolution ...\n";
	 
	 (* liste d'encheres triee *)
	 Mutex.lock data.contents.mut_data;
	 let ordered_list = ordered_bid_list data in
	 begin
	   try
	     List.iteri (fun i a ->
			 let current_username = match data.contents.users.(fst a) with (n,_,_) -> n in
			 if data.contents.active_users.(fst a) && (snd a) != max_int then
			   begin
			     if i<>0 then
			       notify_active_clients data
						     ("MAUVAISE/" ^ current_username ^ "/\n");
			     Mutex.unlock data.contents.mut_data;
			     Thread.delay 60.;
			     Mutex.lock data.contents.mut_data;
			     if data.contents.won then  
			       raise (Winner (fst a))
			     else
			       (* reset game data *)
			       data.contents.game <- game_data_example ()
			   end
			) ordered_list;
	     Mutex.unlock data.contents.mut_data;
	   with Winner(id) ->
	     (* Joueur *)
	     begin
	       notify_active_clients data "BONNE/\n";
	       data.contents.scores.(id) <- data.contents.scores.(id)+1;
	     end
	 end;

	 (* changement de phase *)
	 notify_active_clients data "FINRESO/\n";
	 
	 data.contents.phase <- Reflexion;
	 data.contents.tour <- data.contents.tour + 1;
	 data.contents.won <- false;
	 data.contents.trouve <- false;
	 (* gerer la liste d'attente: rendre touts les status en etat "Playing" *)
	 Array.iteri (fun i user ->
		      match data.contents.active_users.(i) with
			true -> data.contents.user_states.(i) <- Playing
		       |false -> ())
		     data.contents.active_users;
	 (* generer un nouveau plateau *)
	 
       end
  done
    
(* *)
let server_main_thread sock data service =
  let _ = Thread.create game_manager_thread data in 
  while true do     
    let (s, caller) = Unix.accept sock in
    print_endline "new connection";
    let inchan = Unix.in_channel_of_descr s               
    and outchan = Unix.out_channel_of_descr s                
    in
    begin
      let nb_clients = List.length data.contents.clients
      and new_id = new_user_id data in
      data.contents.clients <-
        (Thread.create connection_instance_thread (service,inchan,outchan,s,new_id,data))::data.contents.clients;
      print_endline ("thread pool size " ^ (string_of_int nb_clients));
    end
  done;;

  
  
let main () =
  let max_connections = 10 in
  let data = ref {
                 clients = [];
                 users = (Array.make max_connections ("",stdin,stdout));
                 active_users = (Array.make max_connections false);
		 user_states = Array.make max_connections Not_connected;
                 max_users = max_connections;
		 encheres = Array.make max_connections max_int;
		 scores = Array.make max_connections 0;
		 phase = Waiting_clients;
		 game = game_data_example ();
		 reflexion_duration = 60. *. 5.;
		 auction_duration = 30.;
		 mut_enchere = Mutex.create ();
		 mut_data = Mutex.create ();
		 session_started = false;
		 tour = 0;
		 won = false;
		 trouve = false;
	       }
		 
  and port = 2016 in
  let sock = make_server port max_connections in
  begin
    server_main_thread sock data player_service;
  end

let _ = main ()
