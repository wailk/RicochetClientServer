


all:  clean server


ricochet: ricochet.ml ricochet.mli
	ocamlc -c ricochet.mli ricochet.ml

server: ricochet th_server.ml
	ocamlc -thread unix.cma threads.cma str.cma ricochet.cmo th_server.ml -o $@


test: all
	./server
clean:
	rm -f server *.cmi *.cmo 
