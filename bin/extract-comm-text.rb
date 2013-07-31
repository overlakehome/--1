#!/usr/bin/env ruby
%w{optparse open-uri csv}.each { |e| require e }

$options = {}
OptionParser.new do |opts|
  opts.on('-s', '--headlines <integer>', Integer, 'Skips processing the specified # of headlines (default: 1).') { |v| $options[:headlines] = v }
  opts.on('-m', '--max-lines <integer>', Integer, 'Processes as many as the specified # of lines.') { |v| $options[:max_lines] = v }
  opts.on('-d', '--out-dir <path>', String, 'Specifies an optional output directory path (default: /tmp/).') { |v| $options[:out_dir] = v }
  opts.on('-i', '--id-fields <integer>,...', Array, 'Specifies required field ids to compose output file names.') { |v| $options[:id_fields] = v }
  opts.on('-f', '--fields <integer>,...', Array, 'Specifies required field ids to put into output files.') { |v| $options[:fields] = v }
  opts.on('-x', '--stop-phrases <path>,...', Array, 'Specifies optional file paths for stop-phrases.') { |v| $options[:stop_phrases] = v }
end.parse!

$options[:id_fields] ||= [4]
$options[:fields] ||= [5,12]
fail '--id-fields cannot be nil or empty.' if ($options[:id_fields] || []).empty?
fail '--fields cannot be nil or empty.' if ($options[:fields] || []).empty?

max_lines = $options[:max_lines]
out_dir = $options[:out_dir] || '/tmp/'
id_fields = $options[:id_fields].map { |e| e - 1 }  
fields = $options[:fields].map { |e| e - 1}
stop_phrases = {}
($options[:stop_phrases] || []).each { |e| open(e) { |f| f.each_line { |l| stop_phrases[l.chomp] = nil } } }

ARGV.each do |e|
  open(e) do |f|
    headlines = $options[:headlines] || 1
    CSV.new(f).each do |l|
      next if (headlines -= 1) >= 0
      break unless max_lines.nil? || (max_lines -= 1) >= 0
      path = File.join(out_dir, id_fields.map { |f| l[f] }.join('-') + ".ext")
      puts "WARN: will over-write '#{path}'!!!" if File.exists?(path)
      open(path, 'w') { |w| fields.each { |f| w.puts l[f] } }
      puts "INFO: done writing to '#{path}'."
    end
  end
end
