

(* Unix.gethostname()  *)
let get_my_addr () =
    (Unix.gethostbyname("0.0.0.0")).Unix.h_addr_list.(0)

let main_server  serv_fun =
  try
    (print_string (Unix.gethostname()));
    let port = 12345 in 
    let my_address = get_my_addr()
    in  
	Unix.establish_server serv_fun  (Unix.ADDR_INET(my_address, port))
      
  with
    Failure("int_of_string") -> 
    Printf.eprintf "serv_up : bad port number\n" ;;

  
 let uppercase_service ic oc =
   try while true do    
         let s = input_line ic in print_string s; flush stdout;
         let r = String.uppercase s 
         in output_string oc (r^"\n") ; flush oc
       done
   with _ -> Printf.printf "End of text\n" ; flush stdout ; exit 0 ;;


main_server uppercase_service;;
